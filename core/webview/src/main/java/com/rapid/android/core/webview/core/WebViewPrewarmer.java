package com.rapid.android.core.webview.core;

import android.content.Context;
import android.os.Looper;
import android.os.MessageQueue;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 封装WebView预热逻辑，避免在业务侧重复实现 IdleHandler。
 */
public final class WebViewPrewarmer {

    private static final Set<MessageQueue.IdleHandler> PENDING_TASKS =
            Collections.newSetFromMap(new ConcurrentHashMap<>());

    private WebViewPrewarmer() {}

    /**
     * 在主线程空闲时预热指定数量的WebView，降低首屏创建开销。
     * 不影响冷启动的关键路径。
     */
    @MainThread
    public static void prewarmInIdle(@NonNull Context context, int count) {
        if (count <= 0) {
            return;
        }
        WebViewPoolSupervisor.ensureInitialized(context);
        WebViewPoolMonitor.getInstance().onPrewarm(count);

        MessageQueue queue = Looper.myQueue();
        PrewarmIdleHandler handler = new PrewarmIdleHandler(context.getApplicationContext(), count, queue);
        PENDING_TASKS.add(handler);
        queue.addIdleHandler(handler);
    }

    /**
     * 取消尚未执行的预热任务，避免页面快速销毁导致的资源浪费。
     */
    public static void cancelPendingTasks() {
        MessageQueue mainQueue = Looper.getMainLooper().getQueue();
        for (MessageQueue.IdleHandler handler : PENDING_TASKS) {
            mainQueue.removeIdleHandler(handler);
        }
        PENDING_TASKS.clear();
    }

    private static class PrewarmIdleHandler implements MessageQueue.IdleHandler {
        private final Context appContext;
        private final int count;
        private final MessageQueue queue;

        PrewarmIdleHandler(Context appContext, int count, MessageQueue queue) {
            this.appContext = appContext;
            this.count = count;
            this.queue = queue;
        }

        @Override
        public boolean queueIdle() {
            try {
                WebViewFactory.getInstance(appContext).prewarm(count);
            } catch (Throwable ignored) {
            } finally {
                PENDING_TASKS.remove(this);
                queue.removeIdleHandler(this);
            }
            return false;
        }
    }
}

