package com.rapid.android.core.initializer;

import android.util.Log;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TaskManager
 * - 关键路径阻塞任务阻塞主线程
 * - 非关键路径阻塞任务并行执行
 * - 非阻塞任务按关键路径优先执行
 * - 支持循环依赖检测
 */
public class TaskManager {

    private static final String TAG = "TaskManager";

    private final List<Task> allTasks = new ArrayList<>();
    private final Map<Class<? extends Task>, TaskNode> nodes = new HashMap<>();
    private final List<Throwable> taskExceptions = Collections.synchronizedList(new ArrayList<>());

    private final AtomicInteger criticalBlockingRemaining = new AtomicInteger(0);

    private ExecutorService blockingExecutor;
    private ExecutorService nonBlockingExecutor;

    public TaskManager() {
    }

    public void addTask(Task task) {
        if (nodes.containsKey(task.getClass())) {
            Log.i(TAG, "Task already added: " + task.getName());
            return;
        }
        allTasks.add(task);
    }

    public void addTasks(List<Task> tasks) {
        for (Task task : tasks) addTask(task);
    }

    public void start() {
        long startTime = System.currentTimeMillis();

        buildGraph();
        calculateCriticalPathsAndMarkCriticalBlocking();

        // 初始化线程池
        int cores = Runtime.getRuntime().availableProcessors();
        blockingExecutor = Executors.newFixedThreadPool(Math.max(2, cores / 2));
        nonBlockingExecutor = Executors.newFixedThreadPool(cores);

        // 执行阻塞任务（关键路径和非关键路径一起执行）
        nodes.values().stream()
                .filter(n -> n.remainingDeps.get() == 0 && n.task.getTaskType() == TaskType.BLOCKING)
                .sorted((a, b) -> Integer.compare(b.criticalPath, a.criticalPath))
                .forEach(n -> executeTask(n, n.isCriticalBlocking));

        // 等待关键路径阻塞任务完成
        awaitCriticalBlockingTasks();

        // 执行非阻塞任务（优先级队列）
        PriorityBlockingQueue<TaskNode> queue = new PriorityBlockingQueue<>(nodes.size(),
                Comparator.comparingInt((TaskNode n) -> -n.criticalPath));
        nodes.values().stream()
                .filter(n -> n.remainingDeps.get() == 0 && n.task.getTaskType() != TaskType.BLOCKING)
                .forEach(queue::offer);

        while (!queue.isEmpty()) {
            TaskNode node = queue.poll();
            executeTask(node, false);
        }

        shutdownExecutors();

        long totalDuration = System.currentTimeMillis() - startTime;
        Log.i(TAG, "->Total cost: " + totalDuration + "ms");
        if (!taskExceptions.isEmpty()) {
            Log.e(TAG, "Exceptions occurred during tasks (limited to 10):");
            taskExceptions.stream().limit(10).forEach(t -> Log.e(TAG, t.toString()));
        }
    }

    // ------------------- DAG 构建 -------------------

    private void buildGraph() {
        // 创建节点
        for (Task task : allTasks) {
            nodes.put(task.getClass(), new TaskNode(task));
        }

        // 构建依赖
        for (TaskNode node : nodes.values()) {
            List<Class<? extends Task>> deps = node.task.getDependencies();
            if (deps != null) {
                for (Class<? extends Task> depClass : deps) {
                    TaskNode depNode = nodes.get(depClass);
                    if (depNode != null) {
                        depNode.dependents.add(node);
                        node.remainingDeps.incrementAndGet();
                    } else {
                        throw new IllegalStateException("Dependency not found: " + depClass.getSimpleName());
                    }
                }
            }
        }

        detectCycles();
        printGraph();
    }

    // 循环依赖检测
    private void detectCycles() {
        Set<TaskNode> visited = new HashSet<>();
        Set<TaskNode> stack = new HashSet<>();
        for (TaskNode node : nodes.values()) {
            if (!visited.contains(node)) {
                if (dfsCycle(node, visited, stack)) {
                    throw new IllegalStateException("Cycle detected in tasks!");
                }
            }
        }
    }

    private boolean dfsCycle(TaskNode node, Set<TaskNode> visited, Set<TaskNode> stack) {
        visited.add(node);
        stack.add(node);
        for (TaskNode dep : node.dependents) {
            if (!visited.contains(dep)) {
                if (dfsCycle(dep, visited, stack)) return true;
            } else if (stack.contains(dep)) {
                return true;
            }
        }
        stack.remove(node);
        return false;
    }

    // ------------------- 关键路径计算 & 阻塞任务标记 -------------------

    private void calculateCriticalPathsAndMarkCriticalBlocking() {
        int maxCP = 0;
        Map<TaskNode, Integer> memo = new HashMap<>();
        for (TaskNode node : nodes.values()) {
            int cp = dfsCriticalPath(node, memo);
            node.criticalPath = cp;
            maxCP = Math.max(maxCP, cp);
        }

        // 标记关键路径阻塞任务
        for (TaskNode node : nodes.values()) {
            if (node.task.getTaskType() == TaskType.BLOCKING && node.criticalPath == maxCP) {
                node.isCriticalBlocking = true;
            }
        }
        // 统计关键阻塞任务数量
        criticalBlockingRemaining.set((int) nodes.values().stream()
                .filter(n -> n.isCriticalBlocking).count());
    }

    private int dfsCriticalPath(TaskNode node, Map<TaskNode, Integer> memo) {
        if (memo.containsKey(node)) return memo.get(node);

        int maxDep = 0;
        List<Class<? extends Task>> deps = node.task.getDependencies();
        if (deps != null) {
            for (Class<? extends Task> depClass : deps) {
                TaskNode depNode = nodes.get(depClass);
                if (depNode != null) {
                    maxDep = Math.max(maxDep, dfsCriticalPath(depNode, memo));
                }
            }
        }
        int total = maxDep + node.task.getEstimatedDuration();
        memo.put(node, total);
        return total;
    }

    // ------------------- 任务执行 -------------------

    private void executeTask(TaskNode node, boolean isCriticalBlocking) {
        Runnable runner = () -> {
            long startTime = System.currentTimeMillis();
            try {
                Log.i(TAG, "Start " + node.task.getName() + " | " + node.task.getTaskType());
                node.task.run();
            } catch (Exception e) {
                taskExceptions.add(e);
                Log.e(TAG, "Failed: " + node.task.getName(), e);
            } finally {
                int duration = (int) (System.currentTimeMillis() - startTime);
                Log.i(TAG, "Done " + node.task.getName() + " | " + node.task.getTaskType() + " | Time: " + duration + "ms");

                // 下游任务依赖减1，满足条件就执行
                for (TaskNode dep : node.dependents) {
                    if (dep.remainingDeps.decrementAndGet() == 0) {
                        boolean critical = dep.task.getTaskType() == TaskType.BLOCKING && dep.isCriticalBlocking;
                        executeTask(dep, critical);
                    }
                }

                // 关键路径阻塞任务完成
                if (isCriticalBlocking) {
                    if (criticalBlockingRemaining.decrementAndGet() == 0) {
                        synchronized (criticalBlockingRemaining) {
                            criticalBlockingRemaining.notifyAll();
                        }
                    }
                }
            }
        };

        if (node.task.getTaskType() == TaskType.BLOCKING) {
            blockingExecutor.execute(runner);
        } else {
            nonBlockingExecutor.execute(runner);
        }
    }

    private void awaitCriticalBlockingTasks() {
        synchronized (criticalBlockingRemaining) {
            while (criticalBlockingRemaining.get() > 0) {
                try {
                    criticalBlockingRemaining.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private void shutdownExecutors() {
        shutdownExecutor(blockingExecutor);
        shutdownExecutor(nonBlockingExecutor);
    }

    private void shutdownExecutor(ExecutorService executor) {
        if (executor != null) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    Log.w(TAG, "Some tasks may not finish in time");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // ------------------- DAG 打印 -------------------

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
                " | CP: " + node.criticalPath +
                (node.isCriticalBlocking ? " | CRITICAL" : ""));
        visited.add(node.task.getClass());

        List<TaskNode> children = node.dependents;
        for (int i = 0; i < children.size(); i++) {
            TaskNode child = children.get(i);
            boolean isLast = i == children.size() - 1;
            String childPrefix = prefix + (isLast ? "    └─ " : "    ├─ ");
            printNode(child, childPrefix, visited);
        }
    }

    // ------------------- TaskNode -------------------

    private static class TaskNode {
        final Task task;
        final AtomicInteger remainingDeps = new AtomicInteger(0);
        final List<TaskNode> dependents = new ArrayList<>();
        int criticalPath = 0;
        boolean isCriticalBlocking = false;

        TaskNode(Task task) {
            this.task = task;
        }
    }
}
