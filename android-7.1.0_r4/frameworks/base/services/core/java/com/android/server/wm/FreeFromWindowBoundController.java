package com.android.server.wm;

import static com.android.server.wm.WindowManagerDebugConfig.TAG_WM;

import java.util.ArrayList;

import android.graphics.Rect;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Slog;
import android.view.IDockedStackListener;
import android.view.SurfaceControl;
import android.view.IWindowResizeStackListener;

/**
 * Keeps information about the freeform window bound
 */
public class FreeFromWindowBoundController {
    private final WindowManagerService mService;
    private final DisplayContent mDisplayContent;
    private final Windowboundmark mWindowboundmark;
    private Rect mTaskBoundRect = new Rect();
    private Rect mLastTaskBoundRect = new Rect();

    private final RemoteCallbackList<IWindowResizeStackListener> mWindowResizeStackListener
            = new RemoteCallbackList<>();

    FreeFromWindowBoundController(WindowManagerService service, DisplayContent displayContent) {
        mService = service;
        mDisplayContent = displayContent;

        SurfaceControl.openTransaction();
        try {
            mWindowboundmark = new Windowboundmark(displayContent.getDisplay(), mService.mFxSession);
        } finally {
            SurfaceControl.closeTransaction();
        }
    }

    void setFocusTask(int taskId) {
        ArrayList<TaskStack> stacks = mDisplayContent.getStacks();
        for (int stackNdx = stacks.size() - 1; stackNdx >= 0; --stackNdx) {
            TaskStack stack = stacks.get(stackNdx);
            final ArrayList<Task> tasks = stack.getTasks();
            for (int taskNdx = tasks.size() - 1; taskNdx >= 0; --taskNdx) {
                final Task task = tasks.get(taskNdx);
                final WindowState win = task.getTopVisibleAppMainWindow();
                if (win == null) {
                    continue;
                }

                mLastTaskBoundRect.set(mTaskBoundRect);
                task.getDimBounds(mTaskBoundRect);
            }
        }

        /*
         * if the bound has chenaged,
         * 1. clear the draw before
         * 2. draw new bound
         */
        if (!mTaskBoundRect.equals(mLastTaskBoundRect)) {
            mWindowboundmark.seBoundToDraw(mLastTaskBoundRect, true);
            mWindowboundmark.seBoundToDraw(mTaskBoundRect, false);
        }
        notifyWindowResizeViewVisibilityChanged(true);
    }

    void positionBound(int dw, int dh) {
        mWindowboundmark.positionSurface(dw, dh);
    }

    void notifyWindowResizeViewVisibilityChanged(boolean visible) {
        final int size = mWindowResizeStackListener.beginBroadcast();
        for (int i = 0; i < size; ++i) {
            final IWindowResizeStackListener listener = mWindowResizeStackListener.getBroadcastItem(i);
            try {
                listener.onWindowResizeViewVisibilityChanged(visible);
            } catch (RemoteException e) {
                Slog.e(TAG_WM, "Error delivering divider visibility changed event.", e);
            }
        }
        mWindowResizeStackListener.finishBroadcast();
    }

    public void registerWindowStackStackListener(IWindowResizeStackListener listener) {
        mWindowResizeStackListener.register(listener);
    }
}
