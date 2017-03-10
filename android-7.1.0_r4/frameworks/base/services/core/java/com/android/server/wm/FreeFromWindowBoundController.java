package com.android.server.wm;

import static com.android.server.wm.WindowManagerDebugConfig.TAG_WM;

import java.util.ArrayList;
import android.app.ActivityManager.StackId;
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
    private Task mTask;

    private final Rect mTmpRect = new Rect();

    private final RemoteCallbackList<IWindowResizeStackListener> mWindowResizeStackListener
            = new RemoteCallbackList<>();

    FreeFromWindowBoundController(WindowManagerService service, DisplayContent displayContent) {
        mService = service;
        mDisplayContent = displayContent;
    }

    Task getTask(final int taskId) {
        ArrayList<TaskStack> stacks = mDisplayContent.getStacks();
        for (int stackNdx = stacks.size() - 1; stackNdx >= 0; --stackNdx) {
            TaskStack stack = stacks.get(stackNdx);
            if (StackId.hasWindowDecor(stack.mStackId)) {
                final ArrayList<Task> tasks = stack.getTasks();
                for (Task task : tasks) {
                    if (task.mTaskId == taskId) {
                        return task;
                    }
                }
            }
        }

        return null;
    }

    void setFocusTask(final int taskId) {
        Task task = getTask(taskId);
        if (task != null) {
            final WindowState win = task.getTopVisibleAppMainWindow();
            Rect bound = new Rect();
            mTask = task;
            task.getDimBounds(bound);
            win.dispatchWindowBoundShow(true);
            notifyWindowResizeViewVisibilityChanged(true, bound);
        } else {
            mTask = null;
            Slog.i(TAG_WM, "error, FreeFromWindowBoundController get null task");
        }
    }

    void updateWindowResizeView(Rect frame) {
        if (mTask != null) {
            Rect tempRect1 = new Rect();
            mTask.getBounds(tempRect1);
            mTask.getDimBounds(mTmpRect);
            Slog.i("wangxiaofei", "updateWindowResizeView getBounds: " + tempRect1 + ", getDimBounds: " + mTmpRect);
            frame.set(mTmpRect.right - 20, mTmpRect.bottom - 20, mTmpRect.right + 20, mTmpRect.bottom + 20);
        }
    }

    void notifyWindowResizeViewVisibilityChanged(boolean visible, Rect bound) {
        final int size = mWindowResizeStackListener.beginBroadcast();
        for (int i = 0; i < size; ++i) {
            final IWindowResizeStackListener listener = mWindowResizeStackListener.getBroadcastItem(i);
            try {
                listener.onWindowResizeViewVisibilityChanged(visible, bound);
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
