package com.rapid.android.core.analytics.monitor;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 主线程卡顿检测（带堆栈统计版）
 * 思路：
 * - 利用 Looper.setMessageLogging() 监听主线程消息分发
 * - 当主线程分发开始时启动后台线程定时采样主线程堆栈
 * - 若超出阈值则记录堆栈，统计卡顿区间内堆栈频次
 * - 分发结束时输出最常驻堆栈（卡顿热点）
 */
public class BlockMonitor {

    private static final String TAG = "BlockMonitor";

    /**
     * 卡顿阈值 (ms)
     */
    private static final long BLOCK_THRESHOLD_MS = 700L;
    /**
     * 堆栈采样间隔 (ms)
     */
    private static final long SAMPLE_INTERVAL_MS = 100L;

    private static final BlockMonitor INSTANCE = new BlockMonitor();

    private final HandlerThread watchThread = new HandlerThread("BlockMonitorThread");
    private final Handler watchHandler;
    private final Thread mainThread = Looper.getMainLooper().getThread();
    /**
     * 用于统计堆栈频次
     */
    private final Map<String, Integer> stackFreqMap = new HashMap<>();
    private volatile boolean isDispatching = false;
    private volatile long dispatchStartTime = 0L;
    /**
     * 定时采样堆栈
     */
    private final Runnable sampleRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isDispatching) return;

            long cost = System.currentTimeMillis() - dispatchStartTime;
            if (cost >= BLOCK_THRESHOLD_MS) {
                String stackKey = getCurrentStackKey();
                stackFreqMap.put(stackKey, stackFreqMap.getOrDefault(stackKey, 0) + 1);
            }

            watchHandler.postDelayed(this, SAMPLE_INTERVAL_MS);
        }
    };

    private BlockMonitor() {
        watchThread.start();
        watchHandler = new Handler(watchThread.getLooper());
    }

    public static BlockMonitor getInstance() {
        return INSTANCE;
    }

    public void start() {
        Looper.getMainLooper().setMessageLogging(msg -> {
            if (msg.startsWith(">>>>> Dispatching")) {
                onDispatchStart();
            } else if (msg.startsWith("<<<<< Finished")) {
                onDispatchEnd();
            }
        });
    }

    private void onDispatchStart() {
        isDispatching = true;
        dispatchStartTime = System.currentTimeMillis();
        stackFreqMap.clear();
        startSampling();
    }

    private void onDispatchEnd() {
        isDispatching = false;
        long total = System.currentTimeMillis() - dispatchStartTime;
        if (total >= BLOCK_THRESHOLD_MS) {
            Log.e(TAG, "主线程卡顿结束，总耗时 = " + total + "ms");
            printHotStack();
        }
    }

    /**
     * 启动持续采样
     */
    private void startSampling() {
        watchHandler.postDelayed(sampleRunnable, SAMPLE_INTERVAL_MS);
    }

    /**
     * 将主线程堆栈序列化成字符串作为 key
     */
    private String getCurrentStackKey() {
        StackTraceElement[] stack = mainThread.getStackTrace();
        // 过滤掉无关的系统层堆栈
        return Arrays.stream(stack)
                .filter(e -> !e.getClassName().startsWith("android.")
                        && !e.getClassName().startsWith("java.")
                        && !e.getClassName().startsWith("dalvik.")
                        && !e.getClassName().contains("BlockMonitor"))
                .map(StackTraceElement::toString)
                .reduce("", (a, b) -> a + "\n    at " + b);
    }

    /**
     * 输出出现次数最多的堆栈
     */
    private void printHotStack() {
        if (stackFreqMap.isEmpty()) {
            Log.e(TAG, "没有采样到卡顿堆栈");
            return;
        }

        String hotStack = null;
        int maxCount = 0;
        for (Map.Entry<String, Integer> entry : stackFreqMap.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                hotStack = entry.getKey();
            }
        }

        Log.e(TAG, "最常驻堆栈（出现 " + maxCount + " 次）：" + hotStack);
    }

    public void stop() {
        Looper.getMainLooper().setMessageLogging(null);
        watchThread.quitSafely();
    }
}
