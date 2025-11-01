# 保持其他代码的优化，只对 TaskManager 禁用优化
-dontoptimize class com.rapid.android.core.initializer.TaskManager
-dontoptimize class com.rapid.android.core.initializer.TaskManager$*
-dontoptimize class * implements com.rapid.android.core.initializer.Task

# 保护并发相关的类
-dontoptimize class java.util.concurrent.atomic.AtomicInteger
-dontoptimize class java.lang.Object
