package com.android.systemui.stackdivider;

import static android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
import static android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
import static android.view.WindowManager.LayoutParams.FLAG_SLIPPERY;
import static android.view.WindowManager.LayoutParams.FLAG_SPLIT_TOUCH;
import static android.view.WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
import static android.view.WindowManager.LayoutParams.PRIVATE_FLAG_NO_MOVE_ANIMATION;
import static android.view.WindowManager.LayoutParams.TYPE_WINDOW_RESIZE;

import com.android.systemui.SystemUI;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.misc.SystemServicesProxy;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.view.IWindowResizeStackListener;
import android.view.View;
import android.view.WindowManager;

public class WindowResizeView extends SystemUI {
    private WindowManager.LayoutParams mLp;
    private View mView;
    private static final int DEFAULT_WIDTH = 40;
    private static final int DEFAULT_HEIGHT = 40;
    private final IWindowResizeStackListener mWindowResizeStackListener = new WindowResizeStackListener();

    @Override
    public void start() {
        putComponent(WindowResizeView.class, this);
        SystemServicesProxy ssp = Recents.getSystemServices();
        ssp.registerWindowStackStackListener(mWindowResizeStackListener);
        add(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public void add(int width, int height) {
        WindowManager windowManager = mContext.getSystemService(WindowManager.class);
        View view = new View(mContext);
        view.setBackgroundColor(0xFF0000FF);

        mLp = new WindowManager.LayoutParams(
                width, height, TYPE_WINDOW_RESIZE,
                FLAG_NOT_FOCUSABLE | FLAG_NOT_TOUCH_MODAL
                        | FLAG_WATCH_OUTSIDE_TOUCH | FLAG_SPLIT_TOUCH | FLAG_SLIPPERY,
                PixelFormat.TRANSLUCENT);
        mLp.setTitle("WindowResizeView");
        mLp.privateFlags |= PRIVATE_FLAG_NO_MOVE_ANIMATION;
        view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        windowManager.addView(view, mLp);
        mView = view;
    }

    class WindowResizeStackListener extends IWindowResizeStackListener.Stub {
        @Override
        public void onWindowResizeViewVisibilityChanged(boolean visible) {
            mView.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        }
    }
}
