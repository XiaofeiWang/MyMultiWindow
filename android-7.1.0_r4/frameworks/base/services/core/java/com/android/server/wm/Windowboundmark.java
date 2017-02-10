package com.android.server.wm;

import static com.android.server.wm.WindowManagerDebugConfig.TAG_WM;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Region;
import android.util.Slog;
import android.view.Display;
import android.view.Surface.OutOfResourcesException;
import android.view.Surface;
import android.view.SurfaceControl;
import android.view.SurfaceSession;

public class Windowboundmark {
    private static final int FOCUS_STACK_INFLATE_SIZE = 20;

    private final Display mDisplay;
    private final Paint mBoundPaint;
    private int mLastDW;
    private int mLastDH;

    private final SurfaceControl mSurfaceControl;
    private final Surface mSurface = new Surface();
    private Rect mBound = new Rect();
    private boolean mDrawNeeded;
    private boolean mClearDrawNeeded;

    Windowboundmark(Display display, SurfaceSession session) {
        mDisplay = display;

        mBoundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBoundPaint.setColor(0xffff0000);
        mBoundPaint.setStrokeWidth(1.0f);

        SurfaceControl ctrl = null;
        try {
            ctrl = new SurfaceControl(session, "Windowboundmark",
                    1, 1, PixelFormat.TRANSLUCENT, SurfaceControl.HIDDEN);
            ctrl.setLayerStack(mDisplay.getLayerStack());
            ctrl.setLayer(WindowManagerService.TYPE_LAYER_MULTIPLIER * 102);
            ctrl.setPosition(0, 0);
            ctrl.show();
            mSurface.copyFrom(ctrl);
        } catch (OutOfResourcesException e) {
        }
        mSurfaceControl = ctrl;
    }

    /*
     * @param windowState indicated the current foucst window to draw the bound, if it is null, we will clear
     *        the bound
     */
    void seBoundToDraw(Rect bound, boolean clearDraw) {
        mBound.set(bound);
        mBound.inset(-FOCUS_STACK_INFLATE_SIZE, -FOCUS_STACK_INFLATE_SIZE,
                -FOCUS_STACK_INFLATE_SIZE, -FOCUS_STACK_INFLATE_SIZE);

        if (clearDraw) {
            mClearDrawNeeded = true;
            clearDrawIfNeeded();
        } else {
            mDrawNeeded = true;
            drawIfNeeded();
        }
    }

    void clearDrawIfNeeded() {
        if (mClearDrawNeeded) {
            mClearDrawNeeded = false;
        }
    }

    void positionSurface(int dw, int dh) {
        if (mLastDW == dw && mLastDH == dh) {
            return;
        }
        mLastDW = dw;
        mLastDH = dh;
        mSurfaceControl.setSize(dw, dh);
        mDrawNeeded = true;
    }

    void drawIfNeeded() {
        if (mDrawNeeded) {
            mDrawNeeded = false;

            Canvas c = null;

            try {
                Rect dirty = new Rect(0, 0, mLastDW, mLastDH);
                c = mSurface.lockCanvas(dirty);

                // Top
                c.clipRect(new Rect(mBound.left, mBound.top, mBound.left + mBound.width(), mBound.top + FOCUS_STACK_INFLATE_SIZE), Region.Op.REPLACE);
                c.drawColor(Color.RED);

                // Left
                c.clipRect(new Rect(mBound.left, mBound.top, mBound.left + FOCUS_STACK_INFLATE_SIZE, mBound.bottom), Region.Op.REPLACE);
                c.drawColor(Color.RED);

                // Right
                c.clipRect(new Rect(mBound.right - FOCUS_STACK_INFLATE_SIZE, mBound.top, mBound.right, mBound.bottom), Region.Op.REPLACE);
                c.drawColor(Color.RED);

                // Bottom
                c.clipRect(new Rect(mBound.left,  mBound.bottom - FOCUS_STACK_INFLATE_SIZE, mBound.right, mBound.bottom), Region.Op.REPLACE);
                c.drawColor(Color.RED);

                mSurface.unlockCanvasAndPost(c);
            } catch (Exception e) {
                Slog.e(TAG_WM, "exception, surface = " + mSurface
                        + ", canvas = " + c + ", this = " + this, e);
            }
        }
    }
}
