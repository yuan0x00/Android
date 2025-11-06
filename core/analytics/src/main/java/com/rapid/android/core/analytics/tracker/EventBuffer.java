package com.rapid.android.core.analytics.tracker;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class EventBuffer {

    private static final String TAG = "EventBuffer";
    private BlockingQueue<Event> queue = new LinkedBlockingQueue<>();
    private ExecutorService worker = Executors.newSingleThreadExecutor();
    private boolean isRunning = false;

    public void enqueue(Event event) {
        Log.i(TAG, "Event queued: " + event);
        queue.offer(event);
    }

    public void start() {
        if (isRunning) return;
        isRunning = true;
        worker.execute(() -> {
            while (true) {
                try {
                    List<Event> batch = new ArrayList<>();
                    queue.drainTo(batch, 50);  // 每次最多 50 条
                    if (!batch.isEmpty()) upload(batch);
                    Thread.sleep(5000);        // 每 5 秒上传一次
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void upload(List<Event> events) {
        // 这里可以是 HTTP POST 上报，暂时打印
        Log.i(TAG, "Uploading " + events.size() + " events");
        for (Event event : events) {
//            Log.i(TAG, String.valueOf(event));
        }
    }
}
