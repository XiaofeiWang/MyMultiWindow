package com.android.server.wm;

import static com.android.server.wm.WindowManagerDebugConfig.TAG_WM;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Region;
import android.util.Slog;
import android.view.Display;
import android.view.Surface;
import android.view.Surface.OutOfResourcesException;
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

    void drawWithDirtyRect(final Rect rect) {
        Canvas c = null;

        try {
            c = mSurface.lockCanvas(rect);
            Paint clipPaint = new Paint();
            clipPaint.setAntiAlias(true);
            clipPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            c.drawPaint(clipPaint);

            c.clipRect(rect, Region.Op.REPLACE);
            c.drawColor(Color.RED);
            mSurface.unlockCanvasAndPost(c);
        } catch (Exception e) {
            Slog.e(TAG_WM, "exception, surface = " + mSurface
                    + ", canvas = " + c + ", this = " + this, e);
        }
    }

    void drawIfNeeded() {
        if (mDrawNeeded) {
            mDrawNeeded = false;

            // Top
            Rect topRect = new Rect(mBound.left, mBound.top, mBound.right, mBound.top + FOCUS_STACK_INFLATE_SIZE);
            topRect.intersect(0, 0, mLastDW, mLastDH);
            drawWithDirtyRect(topRect);

            // Left
            Rect leftRect = new Rect(mBound.left - FOCUS_STACK_INFLATE_SIZE , mBound.top - FOCUS_STACK_INFLATE_SIZE , mBound.left + FOCUS_STACK_INFLATE_SIZE, mBound.bottom + FOCUS_STACK_INFLATE_SIZE);
            leftRect.intersect(0, 0, mLastDW, mLastDH);
            drawWithDirtyRect(leftRect);

            // Right
            Rect rightRect = new Rect(mBound.right, mBound.top - FOCUS_STACK_INFLATE_SIZE, mBound.right + FOCUS_STACK_INFLATE_SIZE, mBound.bottom + FOCUS_STACK_INFLATE_SIZE);
            rightRect.intersect(0, 0, mLastDW, mLastDH);
            drawWithDirtyRect(rightRect);

            // Bottom
            Rect bottomRect = new Rect(mBound.left, mBound.bottom, mBound.right, mBound.bottom + FOCUS_STACK_INFLATE_SIZE);
            bottomRect.intersect(0, 0, mLastDW, mLastDH);
            drawWithDirtyRect(bottomRect);
        }
    }
}
