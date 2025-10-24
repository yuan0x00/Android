package com.rapid.android.core.common.app.init;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class TaskManager {
    private static final String TAG = "AppInitManager";

    private final ThreadPoolExecutor ioExecutor;
    private final Handler mainHandler;
    private final ScheduledExecutorService scheduler;
    private final Map<String, InitTask> tasks = new ConcurrentHashMap<>();
    private final Map<String, TaskState> taskStates = new ConcurrentHashMap<>();
    private final Map<String, TaskResult> taskResults = new ConcurrentHashMap<>();
    private final CountDownLatch completionLatch = new CountDownLatch(1);

    private volatile boolean isRunning = false;
    private volatile boolean criticalTasksCompleted = false;
    private InitCallback callback;
    private long startTime;

    public TaskManager() {
        this(createDefaultExecutor());
    }

    public TaskManager(ExecutorService executor) {
        if (executor instanceof ThreadPoolExecutor) {
            this.ioExecutor = (ThreadPoolExecutor) executor;
        } else {
            this.ioExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);
        }
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    private static ThreadPoolExecutor createDefaultExecutor() {
        int availableProcessors = Runtime.getRuntime().availableProcessors();

        // 平衡性能和资源消耗
        int corePoolSize = Math.max(2, availableProcessors);  // 至少2个，建议等于核心数
        int maxPoolSize = Math.min(8, availableProcessors * 2);  // 不超过8个

        return new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                30L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(50),  // 适中队列
                new InitThreadFactory(),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    // 添加单个任务
    public void addTask(InitTask task) {
        if (isRunning) {
            throw new IllegalStateException("Cannot add task after initialization has started");
        }
        tasks.put(task.getName(), task);
        taskStates.put(task.getName(), TaskState.PENDING);
    }

    // 批量添加任务 - 可变参数
    public void addTasks(InitTask... tasks) {
        for (InitTask task : tasks) {
            addTask(task);
        }
    }

    // 批量添加任务 - 列表
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
        this.startTime = System.currentTimeMillis();

        Log.i(TAG, "Starting initialization with " + tasks.size() + " tasks");

        // 打印排序后的任务列表
        printSortedTaskList();

        // 检查循环依赖
        if (hasCircularDependency()) {
            callback.onFailure(new IllegalStateException("Circular dependency detected"));
            return;
        }

        // 检查是否有任务
        if (tasks.isEmpty()) {
            onAllTasksCompleted();
            return;
        }

        // 执行初始化
        executeTasks();
    }

    // 打印排序后的任务列表
    private void printSortedTaskList() {
        if (tasks.isEmpty()) {
            Log.i(TAG, "No tasks to initialize");
            return;
        }

        // 获取所有任务并按优先级排序
        List<InitTask> sortedTasks = tasks.values().stream()
                .sorted((t1, t2) -> Integer.compare(t2.getPriority(), t1.getPriority()))
                .collect(Collectors.toList());

        // 按任务类型分组
        Map<TaskType, List<InitTask>> tasksByType = sortedTasks.stream()
                .collect(Collectors.groupingBy(InitTask::getTaskType));

        Log.i(TAG, "📋 ========== Sorted Task List ==========");
        Log.i(TAG, "Total tasks: " + tasks.size());

        // 打印关键任务
        printTaskGroup("CRITICAL", tasksByType.getOrDefault(TaskType.CRITICAL, Collections.emptyList()));

        // 打印普通任务
        printTaskGroup("NORMAL", tasksByType.getOrDefault(TaskType.NORMAL, Collections.emptyList()));

        // 打印延迟任务
        printTaskGroup("DELAYED", tasksByType.getOrDefault(TaskType.DELAYED, Collections.emptyList()));

        // 打印懒加载任务
        printTaskGroup("LAZY", tasksByType.getOrDefault(TaskType.LAZY, Collections.emptyList()));

        // 打印任务依赖关系
        printTaskDependencies();

        Log.i(TAG, "📋 ======================================");
    }

    private void printTaskGroup(String groupName, List<InitTask> tasks) {
        if (tasks.isEmpty()) return;

        Log.i(TAG, "  ┌─ " + groupName + " Tasks (" + tasks.size() + ")");
        for (int i = 0; i < tasks.size(); i++) {
            InitTask task = tasks.get(i);
            String prefix = (i == tasks.size() - 1) ? "  └── " : "  ├── ";
            Log.i(TAG, prefix + formatTaskInfo(task));
        }
    }

    private String formatTaskInfo(InitTask task) {
        StringBuilder sb = new StringBuilder();
        sb.append(task.getName())
                .append(" [P:")
                .append(task.getPriority())
                .append(", D:");

        if (!task.getDependencies().isEmpty()) {
            sb.append(", Deps: ")
                    .append(String.join(", ", task.getDependencies()));
        }

        if (task.isMainThread()) {
            sb.append(", MainThread");
        }

        sb.append("]");
        return sb.toString();
    }

    private void printTaskDependencies() {
        boolean hasDependencies = tasks.values().stream()
                .anyMatch(task -> !task.getDependencies().isEmpty());

        if (hasDependencies) {
            Log.i(TAG, "  ┌─ Dependency Graph");
            for (InitTask task : tasks.values()) {
                if (!task.getDependencies().isEmpty()) {
                    Log.i(TAG, "  ├── " + task.getName() + " → " +
                            String.join(", ", task.getDependencies()));
                }
            }
            Log.i(TAG, "  └─");
        }
    }

    // 同步等待初始化完成
    public boolean awaitCompletion(long timeout, TimeUnit unit) throws InterruptedException {
        return completionLatch.await(timeout, unit);
    }

    // 强制关闭
    public void shutdown() {
        if (ioExecutor != null && !ioExecutor.isShutdown()) {
            ioExecutor.shutdownNow();
        }
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
    }

    private void executeTasks() {
        List<InitTask> readyTasks = findReadyTasks();

        if (readyTasks.isEmpty() && !isAllTasksCompleted()) {
            // 死锁检测
            List<String> pendingTasks = getPendingTasks();
            Log.e(TAG, "Initialization deadlock detected. Pending tasks: " + pendingTasks);

            if (callback != null) {
                callback.onFailure(new IllegalStateException(
                        "Initialization deadlock detected. Pending tasks: " + pendingTasks));
            }
            return;
        }

        for (InitTask task : readyTasks) {
            executeTaskWithStrategy(task);
        }
    }

    private void executeTaskWithStrategy(InitTask task) {
        switch (task.getTaskType()) {
            case CRITICAL:
            case NORMAL:
                // 关键任务和普通任务立即执行
                executeTaskImmediately(task);
                break;

            case DELAYED:
                // 延迟任务：在关键任务完成后执行
                if (criticalTasksCompleted) {
                    executeTaskImmediately(task);
                } else {
                    scheduleDelayedTask(task, 1000);
                }
                break;

            case LAZY:
                // 懒加载任务：在所有关键任务完成后延迟执行
                if (criticalTasksCompleted) {
                    scheduleDelayedTask(task, 2000);
                }
                break;
        }
    }

    private void executeTaskImmediately(InitTask task) {
        taskStates.put(task.getName(), TaskState.RUNNING);

        Log.d(TAG, "Executing task: " + task.getName() + " | Type: " + task.getTaskType());

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

    private void scheduleDelayedTask(InitTask task, long delayMillis) {
        Log.d(TAG, "Scheduling delayed task: " + task.getName() + " after " + delayMillis + "ms");

        scheduler.schedule(() -> {
            if (taskStates.get(task.getName()) == TaskState.PENDING) {
                executeTaskImmediately(task);
            }
        }, delayMillis, TimeUnit.MILLISECONDS);
    }

    private void onTaskSuccess(InitTask task, long duration) {
        Log.i(TAG, "✅ Task completed: " + task.getName() + " in " + duration + "ms" + " | " + task.getTaskType());

        mainHandler.post(() -> {
            taskStates.put(task.getName(), TaskState.SUCCESS);
            taskResults.put(task.getName(), new TaskResult(task.getName(), TaskState.SUCCESS, duration, null));

            // 检查关键任务是否全部完成
            checkCriticalTasksCompletion();

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

    private void checkCriticalTasksCompletion() {
        if (!criticalTasksCompleted) {
            boolean allCriticalDone = tasks.values().stream()
                    .filter(task -> task.getTaskType() == TaskType.CRITICAL)
                    .allMatch(task -> taskStates.get(task.getName()) == TaskState.SUCCESS);

            if (allCriticalDone) {
                criticalTasksCompleted = true;
                Log.i(TAG, "🎯 All critical tasks completed, starting delayed tasks");

                // 关键任务完成后，触发延迟任务的执行
                executeTasks();
            }
        }
    }

    private void onTaskFailed(InitTask task, Throwable error, long duration) {
        Log.e(TAG, "❌ Task failed: " + task.getName() + " in " + duration + "ms", error);

        mainHandler.post(() -> {
            taskStates.put(task.getName(), TaskState.FAILED);
            taskResults.put(task.getName(), new TaskResult(task.getName(), TaskState.FAILED, duration, error));

            if (task.getTaskType() == TaskType.CRITICAL) {
                // 关键任务失败，终止初始化
                Log.e(TAG, "Critical task failed, stopping initialization: " + task.getName());
                if (callback != null) {
                    callback.onFailure(new RuntimeException("Critical task failed: " + task.getName(), error));
                }
                shutdown();
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
        long totalDuration = System.currentTimeMillis() - startTime;
        Log.i(TAG, "🎉 All initialization tasks completed in " + totalDuration + "ms");
        completionLatch.countDown();

        if (callback != null) {
            List<TaskResult> failedTasks = getFailedTasks();
            if (failedTasks.isEmpty()) {
                callback.onSuccess();
            } else {
                callback.onFailure(new RuntimeException(
                        failedTasks.size() + " tasks failed: " +
                                failedTasks.stream()
                                        .map(TaskResult::getTaskName)
                                        .collect(Collectors.joining(", "))));
            }
        }

        // 温和关闭线程池
        if (ioExecutor != null && !ioExecutor.isShutdown()) {
            ioExecutor.shutdown();
        }
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }

    // 查找可执行的任务（依赖已满足）
    private List<InitTask> findReadyTasks() {
        List<InitTask> readyTasks = new ArrayList<>();

        for (InitTask task : tasks.values()) {
            TaskState currentState = taskStates.get(task.getName());

            if (currentState == TaskState.PENDING &&
                    areDependenciesSatisfied(task) &&
                    isTaskReadyToExecute(task)) {
                readyTasks.add(task);
            }
        }

        // 按优先级排序
        readyTasks.sort((t1, t2) -> Integer.compare(t2.getPriority(), t1.getPriority()));
        return readyTasks;
    }

    private boolean isTaskReadyToExecute(InitTask task) {
        // 根据任务类型判断是否可执行
        switch (task.getTaskType()) {
            case CRITICAL:
            case NORMAL:
                return true; // 关键任务和普通任务立即执行

            case DELAYED:
                return criticalTasksCompleted; // 延迟任务在关键任务完成后执行

            case LAZY:
                return criticalTasksCompleted; // 懒加载任务在关键任务完成后执行

            default:
                return true;
        }
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

    private List<String> getPendingTasks() {
        return taskStates.entrySet().stream()
                .filter(entry -> entry.getValue() == TaskState.PENDING)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private boolean hasCircularDependency() {
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

    public List<TaskResult> getTaskResults() {
        return new ArrayList<>(taskResults.values());
    }

    public long getActiveTaskCount() {
        return taskStates.values().stream()
                .filter(state -> state == TaskState.RUNNING)
                .count();
    }

    public boolean areCriticalTasksCompleted() {
        return criticalTasksCompleted;
    }

    // 获取任务类型统计
    public Map<TaskType, Long> getTaskTypeStatistics() {
        return tasks.values().stream()
                .collect(Collectors.groupingBy(
                        InitTask::getTaskType,
                        Collectors.counting()
                ));
    }

    public interface InitCallback {
        void onProgress(float progress); // 进度回调 0-1

        void onSuccess(); // 所有任务成功完成

        void onFailure(Throwable error); // 初始化失败
    }

    private static class InitThreadFactory implements ThreadFactory {
        private final AtomicInteger threadNumber = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, "InitTask-" + threadNumber.getAndIncrement());
            thread.setPriority(Thread.NORM_PRIORITY - 1);
            thread.setDaemon(false);
            return thread;
        }
    }
}