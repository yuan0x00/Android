package com.rapid.android.core.initializer;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TaskManager: 管理应用冷启动初始化任务。
 * 支持阻塞任务（必须完成后主线程才能继续）和非阻塞任务。
 * 基于 DAG 构建依赖关系，并根据关键路径长度优先执行关键任务。
 */
public class TaskManager {

    private static final String TAG = "TaskManager";
    private static final String PREFS_NAME = "task_times";

    private final List<Task> allTasks = new ArrayList<>();
    private final Map<Class<? extends Task>, TaskNode> nodes = new HashMap<>();
    private final List<Throwable> taskExceptions = Collections.synchronizedList(new ArrayList<>());
    private final AtomicInteger blockingRemaining = new AtomicInteger(0);
    private final SharedPreferences prefs;
    private final Map<Class<? extends Task>, Integer> lastDurations = new HashMap<>();
    private final ExecutorService durationWriter = Executors.newSingleThreadExecutor();
    private ExecutorService nonBlockingExecutor;

    public TaskManager(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * 添加初始化任务
     */
    public void addTask(Task task) {
        allTasks.add(task);
    }

    public void addTasks(List<Task> tasks) {
        for (Task task : tasks) {
            addTask(task);
        }
    }

    /**
     * 启动所有任务
     */
    public void start() {
        long startTime = System.currentTimeMillis();

        buildGraph();
        loadLastDurations();
        calculateCriticalPath();

        // 阻塞任务计数
        blockingRemaining.set((int) nodes.values().stream()
                .filter(n -> n.task.getTaskType() == TaskType.BLOCKING).count());

        int cores = Runtime.getRuntime().availableProcessors();
        nonBlockingExecutor = Executors.newFixedThreadPool(cores);

        // 执行阻塞任务（按关键路径降序）
        long blockingStart = System.currentTimeMillis();
        nodes.values().stream()
                .filter(n -> n.remainingDeps.get() == 0 && n.task.getTaskType() == TaskType.BLOCKING)
                .sorted((a, b) -> Integer.compare(b.criticalPath, a.criticalPath))
                .forEach(n -> executeTask(n, true));
        awaitBlockingTasks();
        long blockingTasksTime = System.currentTimeMillis() - blockingStart;

        // 执行非阻塞任务（按关键路径降序）
        long nonBlockingStart = System.currentTimeMillis();
        nodes.values().stream()
                .filter(n -> n.remainingDeps.get() == 0 && n.task.getTaskType() != TaskType.BLOCKING)
                .sorted((a, b) -> Integer.compare(b.criticalPath, a.criticalPath))
                .forEach(n -> executeTask(n, false));
        shutdownNonBlockingExecutor();
        long nonBlockingTasksTime = System.currentTimeMillis() - nonBlockingStart;
        long totalDuration = System.currentTimeMillis() - startTime;

        Log.i(TAG, "->Blocking tasks completed | Time: " + blockingTasksTime + "ms");
        Log.i(TAG, "->Non-Blocking tasks completed | Time: " + nonBlockingTasksTime + "ms");
        Log.i(TAG, "->Total cost: " + totalDuration + "ms");

        if (!taskExceptions.isEmpty()) {
            Log.e(TAG, "Exceptions occurred during tasks (limited to 10):");
            taskExceptions.stream().limit(10).forEach(t -> Log.e(TAG, t.toString()));
        }
    }

    /**
     * 构建任务 DAG
     */
    private void buildGraph() {

        // 创建节点
        for (Task task : allTasks) {
            nodes.put(task.getClass(), new TaskNode(task));
        }

        // 构建依赖
        for (TaskNode node : nodes.values()) {
            List<Class<? extends Task>> deps = node.task.getDependencies();
            if (deps != null) {
                for (Class<? extends Task> dep : deps) {
                    TaskNode depNode = nodes.get(dep);
                    if (depNode != null) {
                        depNode.dependents.add(node);
                        node.remainingDeps.incrementAndGet();
                    } else {
                        throw new IllegalStateException("Dependency not found: " + dep.getSimpleName());
                    }
                }
            }
        }

        printGraph();
    }

    /**
     * 打印 DAG 树状结构，显示关键路径长度
     */
    private void printGraph() {
        Log.i(TAG, "---Task DAG Tree Structure---");
        List<TaskNode> roots = new ArrayList<>();
        for (TaskNode node : nodes.values()) {
            if (node.remainingDeps.get() == 0) roots.add(node);
        }
        Set<Class<? extends Task>> visited = new HashSet<>();
        for (TaskNode root : roots) {
            printNode(root, "", visited);
        }
        Log.i(TAG, "---Task DAG Tree Structure---");
    }

    private void printNode(TaskNode node, String prefix, Set<Class<? extends Task>> visited) {
        if (visited.contains(node.task.getClass())) {
            Log.i(TAG, prefix + node.task.getName() + " (↩)");
            return;
        }
        Log.i(TAG, prefix + node.task.getName() +
                " | " + node.task.getTaskType() +
                " | CP: " + node.criticalPath);
        visited.add(node.task.getClass());

        List<TaskNode> children = node.dependents;
        for (int i = 0; i < children.size(); i++) {
            TaskNode child = children.get(i);
            boolean isLast = i == children.size() - 1;
            String childPrefix = prefix + (isLast ? "    └─ " : "    ├─ ");
            printNode(child, childPrefix, visited);
        }
    }

    /**
     * 加载上次任务耗时，用于关键路径计算
     */
    private void loadLastDurations() {
        lastDurations.clear();
        for (TaskNode node : nodes.values()) {
            int duration = prefs.getInt(node.task.getClass().getName(), node.task.getEstimatedDuration());
            lastDurations.put(node.task.getClass(), duration);
        }
    }

    /**
     * 计算关键路径长度（基于历史耗时）
     */
    private void calculateCriticalPath() {
        Map<TaskNode, Integer> memo = new HashMap<>();
        for (TaskNode node : nodes.values()) {
            node.criticalPath = dfsCriticalPath(node, memo);
        }
    }

    private int dfsCriticalPath(TaskNode node, Map<TaskNode, Integer> memo) {
        if (memo.containsKey(node)) return Objects.requireNonNull(memo.get(node));
        int maxDep = 0;
        for (TaskNode dep : node.dependents) {
            maxDep = Math.max(maxDep, dfsCriticalPath(dep, memo));
        }
        int duration = lastDurations.getOrDefault(node.task.getClass(), node.task.getEstimatedDuration());
        int total = maxDep + duration;
        memo.put(node, total);
        return total;
    }

    /**
     * 执行任务统一逻辑
     */
    private void executeTask(TaskNode node, boolean isBlocking) {
        Runnable runner = () -> {
            long startTime = System.currentTimeMillis();
            try {
//                Log.i(TAG, "Start " + node.task.getName() + " | " + node.task.getTaskType());
                node.task.run();
            } catch (Exception e) {
                taskExceptions.add(e);
                Log.e(TAG, "Failed: " + node.task.getName(), e);
            } finally {
                int duration = (int) (System.currentTimeMillis() - startTime);
                lastDurations.put(node.task.getClass(), duration);
                saveDurationAsync(node.task.getClass(), duration);

                if (isBlocking) {
                    if (blockingRemaining.decrementAndGet() == 0) {
                        synchronized (blockingRemaining) {
                            blockingRemaining.notifyAll();
                        }
                    }
                }

                for (TaskNode dep : node.dependents) {
                    if (dep.remainingDeps.decrementAndGet() == 0) {
                        executeTask(dep, dep.task.getTaskType() == TaskType.BLOCKING);
                    }
                }

                Log.i(TAG, "Done " + node.task.getName() + " | " + node.task.getTaskType() + " | Time: " + duration + "ms");
            }
        };

        if (isBlocking) runner.run();
        else nonBlockingExecutor.execute(runner);
    }

    /**
     * 异步保存任务耗时到 SharedPreferences
     */
    private void saveDurationAsync(Class<? extends Task> clazz, int duration) {
        durationWriter.execute(() -> prefs.edit().putInt(clazz.getName(), duration).apply());
    }

    /**
     * 阻塞任务等待
     */
    private void awaitBlockingTasks() {
        synchronized (blockingRemaining) {
            while (blockingRemaining.get() > 0) {
                try {
                    blockingRemaining.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    /**
     * 关闭非阻塞任务线程池，并等待耗时写入完成
     */
    private void shutdownNonBlockingExecutor() {
        if (nonBlockingExecutor != null) {
            nonBlockingExecutor.shutdown();
            try {
                if (!nonBlockingExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                    Log.w(TAG, "Some non-blocking tasks may not finish in time");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        durationWriter.shutdown();
        try {
            if (!durationWriter.awaitTermination(5, TimeUnit.SECONDS)) {
                Log.w(TAG, "Some task durations may not be saved in time");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * DAG 节点
     */
    private static class TaskNode {
        final Task task;
        final AtomicInteger remainingDeps = new AtomicInteger(0);
        final List<TaskNode> dependents = new ArrayList<>();
        int criticalPath = 0;

        TaskNode(Task task) {
            this.task = task;
        }
    }
}
