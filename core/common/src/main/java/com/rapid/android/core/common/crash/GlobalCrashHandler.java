package com.rapid.android.core.common.crash;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.rapid.android.core.log.LogKit;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 全局崩溃处理器。调用 {@link #install(Context)} 后自动接管应用未捕获异常，
 * 负责本地落盘、可选上传以及进程退出前的兜底处理。
 */
public final class GlobalCrashHandler implements Thread.UncaughtExceptionHandler {

    private static final String CRASH_DIR_NAME = "crash_logs";

    private static volatile GlobalCrashHandler sInstance;
    private static volatile CrashReporter crashReporter;

    private final Context appContext;
    private final Handler mainHandler;
    private Thread.UncaughtExceptionHandler defaultHandler;

    private GlobalCrashHandler(@NonNull Context context) {
        this.appContext = context.getApplicationContext();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * 安装全局崩溃处理器。建议在 {@code Application.onCreate()} 中调用。
     */
    public static void install(@NonNull Context context) {
        if (sInstance == null) {
            synchronized (GlobalCrashHandler.class) {
                if (sInstance == null) {
                    sInstance = new GlobalCrashHandler(context);
                    sInstance.init();
                }
            }
        }
    }

    public static boolean isInstalled() {
        return sInstance != null;
    }

    public static void setCrashReporter(@Nullable CrashReporter reporter) {
        crashReporter = reporter;
    }

    private void init() {
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(@NonNull Thread thread, @NonNull Throwable ex) {
        try {
            File crashFile = saveCrashInfoToFile(ex);
            if (crashFile != null) {
                safeLog("App crashed, log saved to: " + crashFile.getAbsolutePath());
            } else {
                safeLog("App crashed, save log failed");
            }

            uploadCrashLog(crashFile, ex);

            if (isMainProcess()) {
                showCrashDialog(crashFile);
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }

        } catch (Throwable t) {
            safeLog("!!! GlobalCrashHandler crashed !!!" + t.getMessage());
        } finally {
            killProcess();
        }
    }

    private void killProcess() {
        try {
            if (defaultHandler != null) {
                defaultHandler.uncaughtException(Thread.currentThread(),
                        new RuntimeException("Terminated by GlobalCrashHandler"));
            }
        } catch (Throwable ignored) {
        }
        Process.killProcess(Process.myPid());
        System.exit(10);
    }

    private @Nullable File saveCrashInfoToFile(@NonNull Throwable ex) {
        Writer writer = null;
        BufferedWriter bufferedWriter = null;
        try {
            StringBuilder sb = new StringBuilder();
            appendDeviceInfo(sb);
            appendExceptionInfo(sb, ex);

            File dir = getCrashDir();
            if (dir == null) {
                return null;
            }
            File file = new File(dir, "crash-" + getTimestamp() + ".log");

            writer = new FileWriter(file);
            bufferedWriter = new BufferedWriter(writer);
            bufferedWriter.write(sb.toString());
            bufferedWriter.flush();
            return file;
        } catch (IOException e) {
            safeLog("write crash log failed: " + e.getMessage());
            return null;
        } finally {
            try {
                if (bufferedWriter != null) bufferedWriter.close();
                if (writer != null) writer.close();
            } catch (IOException e) {
                safeLog("close writer failed: " + e.getMessage());
            }
        }
    }

    private void appendDeviceInfo(@NonNull StringBuilder sb) {
        sb.append("======== Device Info ========\n");
        sb.append("Brand: ").append(safeToString(Build.BRAND)).append("\n");
        sb.append("Model: ").append(safeToString(Build.MODEL)).append("\n");
        sb.append("Manufacturer: ").append(safeToString(Build.MANUFACTURER)).append("\n");
        sb.append("SDK: ").append(Build.VERSION.SDK_INT).append("\n");
        sb.append("Android: ").append(Build.VERSION.RELEASE).append("\n");
        sb.append("Locale: ").append(getCurrentLocale()).append("\n");
        sb.append("App Version: ").append(getAppVersionName()).append(" (" + getAppVersionCode() + ")\n");
        sb.append("Process Name: ").append(getProcessName(appContext)).append("\n\n");
    }

    private void appendExceptionInfo(@NonNull StringBuilder sb, @NonNull Throwable ex) {
        sb.append("======== Exception Info ========\n");
        sb.append("Exception: ").append(safeToString(ex)).append("\n\n");

        StringWriter sw = null;
        PrintWriter pw = null;
        try {
            sw = new StringWriter();
            pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            sb.append(sw);
        } finally {
            if (pw != null) pw.close();
            if (sw != null) try {
                sw.close();
            } catch (IOException ignored) {
            }
        }
    }

    private String getTimestamp() {
        return new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
    }

    private String getAppVersionName() {
        try {
            return appContext.getPackageManager()
                    .getPackageInfo(appContext.getPackageName(), 0)
                    .versionName;
        } catch (Exception e) {
            return "unknown";
        }
    }

    private long getAppVersionCode() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                return appContext.getPackageManager()
                        .getPackageInfo(appContext.getPackageName(), 0)
                        .getLongVersionCode();
            }
            return appContext.getPackageManager()
                    .getPackageInfo(appContext.getPackageName(), 0)
                    .versionCode;
        } catch (Exception e) {
            return 0L;
        }
    }

    private Locale getCurrentLocale() {
        return appContext.getResources().getConfiguration().locale;
    }

    private String getProcessName(Context context) {
        try {
            int pid = Process.myPid();
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> runningApps = am != null ? am.getRunningAppProcesses() : null;
            if (runningApps != null) {
                for (ActivityManager.RunningAppProcessInfo procInfo : runningApps) {
                    if (procInfo.pid == pid) {
                        return procInfo.processName;
                    }
                }
            }
        } catch (Exception e) {
            safeLog("getProcessName failed: " + e.getMessage());
        }
        return "unknown";
    }

    private @Nullable File getCrashDir() {
        try {
            File dir = new File(appContext.getExternalFilesDir(null), CRASH_DIR_NAME);
            if (!dir.exists() && !dir.mkdirs()) {
                safeLog("create crash dir failed: " + dir.getAbsolutePath());
                return null;
            }
            return dir;
        } catch (Exception e) {
            safeLog("getCrashDir failed: " + e.getMessage());
            return null;
        }
    }

    private boolean isMainProcess() {
        try {
            String processName = getProcessName(appContext);
            String packageName = appContext.getPackageName();
            return TextUtils.equals(processName, packageName);
        } catch (Exception e) {
            safeLog("isMainProcess failed: " + e.getMessage());
            return false;
        }
    }

    private void uploadCrashLog(@Nullable File crashFile, @NonNull Throwable ex) {
        CrashReporter reporter = crashReporter;
        if (reporter == null) {
            return;
        }
        try {
            reporter.report(crashFile, ex);
        } catch (Exception e) {
            safeLog("upload crash log failed: " + e.getMessage());
        }
    }

    private void showCrashDialog(@Nullable File crashFile) {
        mainHandler.post(() -> {
            // 预留扩展：可启动错误页面或通知用户
            // 目前仅保留 hook，避免在崩溃时再次触发异常
        });
    }

    private void safeLog(String msg) {
        if (msg == null) return;
        try {
            LogKit.e("CrashHandler", "[CrashHandler] %s", msg);
        } catch (Throwable t) {
            System.err.println("[GlobalCrashHandler] " + msg);
        }
    }

    private String safeToString(Object obj) {
        if (obj == null) {
            return "null";
        }
        try {
            return obj.toString();
        } catch (Throwable t) {
            return "toString_failed";
        }
    }

    public interface CrashReporter {
        void report(@Nullable File crashFile, @NonNull Throwable throwable) throws Exception;
    }
}
