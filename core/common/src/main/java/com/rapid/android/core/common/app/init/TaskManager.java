package com.rapid.android.core.common.app.init;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class TaskManager {
    private static final String TAG = "AppInitManager";

    private final ExecutorService ioExecutor;
    private final Handler mainHandler;
    private final Map<String, InitTask> tasks = new ConcurrentHashMap<>();
    private final Map<String, TaskState> taskStates = new ConcurrentHashMap<>();
    private final Map<String, TaskResult> taskResults = new ConcurrentHashMap<>();
    private final CountDownLatch completionLatch = new CountDownLatch(1);

    private volatile boolean isRunning = false;
    private InitCallback callback;

    public TaskManager() {
        this(Executors.newFixedThreadPool(4));
    }

    public TaskManager(ExecutorService executor) {
        this.ioExecutor = executor;
        this.mainHandler = new Handler(Looper.getMainLooper());
    }


    // 添加任务
    public void addTask(InitTask task) {
        if (isRunning) {
            throw new IllegalStateException("Cannot add task after initialization has started");
        }
        tasks.put(task.getName(), task);
        taskStates.put(task.getName(), TaskState.PENDING);
    }

    // 批量添加任务
    public void addTasks(List<InitTask> tasks) {
        for (InitTask task : tasks) {
            addTask(task);
        }
    }

    // 启动初始化
    public void start(InitCallback callback) {
        if (isRunning) {
            throw new IllegalStateException("Initialization is already running");
        }

        this.isRunning = true;
        this.callback = callback;

        Log.i(TAG, "Starting initialization with " + tasks.size() + " tasks");

        // 检查循环依赖
        if (hasCircularDependency()) {
            callback.onFailure(new IllegalStateException("Circular dependency detected"));
            return;
        }

        // 执行初始化
        executeTasks();
    }

    // 同步等待初始化完成
    public boolean awaitCompletion(long timeout, TimeUnit unit) throws InterruptedException {
        return completionLatch.await(timeout, unit);
    }

    private void executeTasks() {
        List<InitTask> readyTasks = findReadyTasks();

        if (readyTasks.isEmpty() && !isAllTasksCompleted()) {
            // 死锁检测：有任务未完成但没有可执行的任务
//            callback.onFailure(new IllegalStateException("Initialization deadlock detected"));
            return;
        }

        for (InitTask task : readyTasks) {
            executeTask(task);
        }
    }

    private void executeTask(InitTask task) {
        taskStates.put(task.getName(), TaskState.RUNNING);

        Log.d(TAG, "Executing task: " + task.getName());

        long startTime = System.currentTimeMillis();

        Runnable taskRunnable = () -> {
            try {
                task.execute();
                long duration = System.currentTimeMillis() - startTime;
                onTaskSuccess(task, duration);
            } catch (Exception e) {
                long duration = System.currentTimeMillis() - startTime;
                onTaskFailed(task, e, duration);
            }
        };

        if (task.isMainThread()) {
            mainHandler.post(taskRunnable);
        } else {
            ioExecutor.execute(taskRunnable);
        }
    }

    private void onTaskSuccess(InitTask task, long duration) {
        Log.i(TAG, "Task completed: " + task.getName() + " in " + duration + "ms");

        mainHandler.post(() -> {
            taskStates.put(task.getName(), TaskState.SUCCESS);
            taskResults.put(task.getName(), new TaskResult(task.getName(), TaskState.SUCCESS, duration, null));

            // 通知进度
            if (callback != null) {
                callback.onProgress(getProgress());
            }

            // 检查是否所有任务完成
            if (isAllTasksCompleted()) {
                onAllTasksCompleted();
            } else {
                // 执行下一批任务
                executeTasks();
            }
        });
    }

    private void onTaskFailed(InitTask task, Throwable error, long duration) {
        Log.e(TAG, "Task failed: " + task.getName(), error);

        mainHandler.post(() -> {
            taskStates.put(task.getName(), TaskState.FAILED);
            taskResults.put(task.getName(), new TaskResult(task.getName(), TaskState.FAILED, duration, error));

            if (task.isCritical()) {
                // 关键任务失败，终止初始化
                if (callback != null) {
                    callback.onFailure(new RuntimeException("Critical task failed: " + task.getName(), error));
                }
            } else {
                // 非关键任务失败，继续执行
                if (callback != null) {
                    callback.onProgress(getProgress());
                }

                if (isAllTasksCompleted()) {
                    onAllTasksCompleted();
                } else {
                    executeTasks();
                }
            }
        });
    }

    private void onAllTasksCompleted() {
        Log.i(TAG, "All initialization tasks completed");
        completionLatch.countDown();

        if (callback != null) {
            List<TaskResult> failedTasks = getFailedTasks();
            if (failedTasks.isEmpty()) {
                callback.onSuccess();
            } else {
                callback.onFailure(new RuntimeException(failedTasks.size() + " tasks failed"));
            }
        }

        // 清理资源
        ioExecutor.shutdown();
    }

    // 查找可执行的任务（依赖已满足）
    private List<InitTask> findReadyTasks() {
        List<InitTask> readyTasks = new ArrayList<>();

        for (InitTask task : tasks.values()) {
            if (taskStates.get(task.getName()) == TaskState.PENDING &&
                    areDependenciesSatisfied(task)) {
                readyTasks.add(task);
            }
        }

        // 按优先级排序
        readyTasks.sort((t1, t2) -> Integer.compare(t2.getPriority(), t1.getPriority()));
        return readyTasks;
    }

    private boolean areDependenciesSatisfied(InitTask task) {
        for (String dependency : task.getDependencies()) {
            TaskState state = taskStates.get(dependency);
            if (state != TaskState.SUCCESS) {
                return false;
            }
        }
        return true;
    }

    private boolean isAllTasksCompleted() {
        for (TaskState state : taskStates.values()) {
            if (state != TaskState.SUCCESS && state != TaskState.FAILED) {
                return false;
            }
        }
        return true;
    }

    private boolean hasCircularDependency() {
        // 简化的循环依赖检测
        // 实际项目中可以使用拓扑排序
        for (InitTask task : tasks.values()) {
            if (hasCircularDependency(task.getName(), new HashSet<>())) {
                return true;
            }
        }
        return false;
    }

    private boolean hasCircularDependency(String taskName, Set<String> visited) {
        if (visited.contains(taskName)) {
            return true;
        }

        visited.add(taskName);
        InitTask task = tasks.get(taskName);
        if (task != null) {
            for (String dependency : task.getDependencies()) {
                if (hasCircularDependency(dependency, new HashSet<>(visited))) {
                    return true;
                }
            }
        }

        return false;
    }

    public float getProgress() {
        int completed = 0;
        for (TaskState state : taskStates.values()) {
            if (state == TaskState.SUCCESS || state == TaskState.FAILED) {
                completed++;
            }
        }
        return tasks.isEmpty() ? 1.0f : (float) completed / tasks.size();
    }

    public List<TaskResult> getFailedTasks() {
        return taskResults.values().stream()
                .filter(result -> result.getState() == TaskState.FAILED)
                .collect(Collectors.toList());
    }

    public Map<String, TaskState> getTaskStates() {
        return new HashMap<>(taskStates);
    }

    public interface InitCallback {
        void onProgress(float progress); // 进度回调 0-1
        void onSuccess(); // 所有任务成功完成
        void onFailure(Throwable error); // 初始化失败
    }
}

