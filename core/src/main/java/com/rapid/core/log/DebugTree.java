package com.rapid.core.log;

import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.PrintWriter;
import java.io.StringWriter;

import timber.log.Timber;

public class DebugTree extends Timber.Tree {

    private static final String[] FRAMEWORK_CLASSES = {
            "timber.log.Timber",
            "com.rapid.core.log.CoreLogger",
            "com.rapid.core.log.DebugTree",
            "android.",
            "java.",
            "androidx.",
            "dalvik.",
            "sun.",
            "com.android.",
            "org.apache.",
            "kotlin.",
            "com.jakewharton.timber"
    };

    @Override
    protected void log(int priority, @Nullable String tag, @NonNull String message, @Nullable Throwable t) {
        // 获取调用栈元素
        StackTraceElement element = findLoggingElement(Thread.currentThread().getStackTrace());
        String fileLineInfo = "";
        String simpleTag = "Unknown";

        if (element != null) {
            String className = element.getClassName();
            String simpleName = className.substring(className.lastIndexOf('.') + 1);
            int lineNumber = element.getLineNumber();

            // 移除匿名类后缀
            int dollarIndex = simpleName.lastIndexOf('$');
            if (dollarIndex != -1) {
                simpleName = simpleName.substring(0, dollarIndex);
            }

            // 构造 Logcat 可识别的超链接格式（放在 message 开头）
            fileLineInfo = String.format("%s.java:%d: ", simpleName, lineNumber);

            // TAG 使用简化类名（可选）
            simpleTag = simpleName;

            // 限制 TAG 长度（Android < 8.0 限制 23 字符）
            if (simpleTag.length() > 23 && Build.VERSION.SDK_INT < 26) {
                simpleTag = simpleTag.substring(0, 23);
            }
        }

        // 如果传入了 tag，优先使用
        if (tag != null) {
            simpleTag = tag;
        }

        // 拼接堆栈信息
        if (t != null) {
            message += "\n" + t + "\n" + getStackTraceString(t);
        }

        // 将文件行号信息加到 message 开头，让 Logcat 识别为超链接
        message = fileLineInfo + message;

        // 输出日志
        switch (priority) {
            case Log.VERBOSE:
                Log.v(simpleTag, message);
                break;
            case Log.DEBUG:
                Log.d(simpleTag, message);
                break;
            case Log.INFO:
                Log.i(simpleTag, message);
                break;
            case Log.WARN:
                Log.w(simpleTag, message);
                break;
            case Log.ERROR:
                Log.e(simpleTag, message);
                break;
            case Log.ASSERT:
                Log.wtf(simpleTag, message);
                break;
            default:
                Log.println(priority, simpleTag, message);
                break;
        }
    }

    private StackTraceElement findLoggingElement(StackTraceElement[] stackTrace) {
        for (StackTraceElement element : stackTrace) {
            String className = element.getClassName();
            if (!isFrameworkClass(className)) {
                return element;
            }
        }
        return null;
    }

    private boolean isFrameworkClass(String className) {
        for (String prefix : FRAMEWORK_CLASSES) {
            if (className.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private String getStackTraceString(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        pw.flush();
        return sw.toString();
    }
}