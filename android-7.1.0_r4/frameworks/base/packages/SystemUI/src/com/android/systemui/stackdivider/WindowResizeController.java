package com.android.systemui.stackdivider;

import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import com.android.systemui.SystemUI;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.misc.SystemServicesProxy;

import android.view.IWindowResizeStackListener;

public class WindowResizeController extends SystemUI {
    private WindowManager.LayoutParams mWindowParams;
    private View mView;
    WindowManager mWindowManager;

    private boolean mVisible;
    private static final int DEFAULT_WIDTH = 40;
    private static final int DEFAULT_HEIGHT = 40;
    private final IWindowResizeStackListener mWindowResizeStackListener = new WindowResizeStackListener();

    @Override
    public void start() {
        putComponent(WindowResizeController.class, this);
        SystemServicesProxy ssp = Recents.getSystemServices();
        ssp.registerWindowStackStackListener(mWindowResizeStackListener);
        mWindowManager = mContext.getSystemService(WindowManager.class);
        add(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    void add(int width, int height) {
        View view = new View(mContext);
        view.setBackgroundColor(0xFF0000FF);
        view.setVisibility(View.GONE);
        mVisible = false;

        mWindowParams = new WindowManager.LayoutParams(
                width, height, WindowManager.LayoutParams.TYPE_WINDOW_RESIZE,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);
        mWindowParams.setTitle("WindowResizeView");

        mWindowParams.gravity = Gravity.CENTER;
        mWindowManager.addView(view, mWindowParams);
        mView = view;
    }

    void updateWindow(final boolean visible, final Rect bound) {
        mView.post(new Runnable() {
            @Override
            public void run() {
                if (mVisible != visible) {
                    mVisible = visible ;
                    mView.setVisibility(visible ? View.VISIBLE : View.GONE);
                }
            }
        });
    }

    class WindowResizeStackListener extends IWindowResizeStackListener.Stub {
        @Override
        public void onWindowResizeViewVisibilityChanged(boolean visible, Rect bound) {
            updateWindow(visible, bound);
        }
    }
}
