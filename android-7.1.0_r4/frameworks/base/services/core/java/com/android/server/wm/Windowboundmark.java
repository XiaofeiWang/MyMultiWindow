package com.android.server.wm;

import static com.android.server.wm.WindowManagerDebugConfig.TAG_WM;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
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

    private final SurfaceControl mSurfaceControl;
    private final Surface mSurface = new Surface();
    private Rect mLastBound = new Rect();
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

    void positionSurface(int dw, int dh) {
        //mSurfaceControl.setSize(dw, dh);
        mDrawNeeded = true;
    }

    /*
     * @param windowState indicated the current foucst window to draw the bound, if it is null, we will clear
     *        the bound
     */
    void setWindowToDraw(WindowState windowState) {
        if (windowState == null) {
            mClearDrawNeeded = true;
            clearDrawIfNeeded();
        } else {
            mLastBound.set(mBound);
            mBound.set(windowState.getFrameLw());
            mBound.inset(-FOCUS_STACK_INFLATE_SIZE, -FOCUS_STACK_INFLATE_SIZE,
                    -FOCUS_STACK_INFLATE_SIZE, -FOCUS_STACK_INFLATE_SIZE);
            mDrawNeeded = true;
            drawIfNeeded();
        }
    }

    void clearDrawIfNeeded() {
        if (mClearDrawNeeded) {
            mClearDrawNeeded = false;
        }
    }

    void drawIfNeeded() {
        if (mDrawNeeded) {
            mDrawNeeded = false;

            Canvas c = null;

            try {
                c = mSurface.lockCanvas(mBound);
                c.drawLine(mBound.left, mBound.top, mBound.right, mBound.top, mBoundPaint);
                c.drawLine(mBound.left, mBound.top, mBound.left, mBound.bottom, mBoundPaint);
                c.drawLine(mBound.left, mBound.bottom, mBound.right, mBound.bottom, mBoundPaint);
                c.drawLine(mBound.right, mBound.top, mBound.right, mBound.bottom, mBoundPaint);
                mSurface.unlockCanvasAndPost(c);
            } catch (Exception e) {
                Slog.e(TAG_WM, "exception, surface = " + mSurface
                        + ", canvas = " + c + ", this = " + this, e);
            }
        }
    }
}
