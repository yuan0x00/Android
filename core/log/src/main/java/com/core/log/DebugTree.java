package com.core.log;

import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.PrintWriter;
import java.io.StringWriter;

import timber.log.Timber;

final class DebugTree extends Timber.Tree {

    private static final String[] FRAMEWORK_PREFIX = {
            "timber.log.Timber",
            "com.core.log.LogKit",
            "com.core.log.DebugTree",
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
        StackTraceElement element = findCaller(Thread.currentThread().getStackTrace());
        String fileLineInfo = "";
        String simpleTag = tag;

        if (element != null) {
            String className = element.getClassName();
            String simpleName = className.substring(className.lastIndexOf('.') + 1);
            int dollarIndex = simpleName.lastIndexOf('$');
            if (dollarIndex != -1) {
                simpleName = simpleName.substring(0, dollarIndex);
            }
            int lineNumber = element.getLineNumber();
            fileLineInfo = String.format("%s.java:%d: ", simpleName, lineNumber);

            if (simpleTag == null || simpleTag.isEmpty()) {
                simpleTag = simpleName;
            }
        }

        if (simpleTag == null || simpleTag.isEmpty()) {
            simpleTag = "LogKit";
        }

        if (simpleTag.length() > 23 && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            simpleTag = simpleTag.substring(0, 23);
        }

        if (t != null) {
            message += "\n" + getStackTraceString(t);
        }

        message = fileLineInfo + message;

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

    private StackTraceElement findCaller(@NonNull StackTraceElement[] stackTrace) {
        for (StackTraceElement element : stackTrace) {
            if (!isPlatformClass(element.getClassName())) {
                return element;
            }
        }
        return null;
    }

    private boolean isPlatformClass(@NonNull String className) {
        for (String prefix : FRAMEWORK_PREFIX) {
            if (className.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private String getStackTraceString(@NonNull Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        pw.flush();
        return sw.toString();
    }
}
