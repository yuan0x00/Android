package com.core.common.crash;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Process;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.core.CoreApp;
import com.core.common.log.Logger;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 全局崩溃处理器（使用 CoreApp + CoreLogger 版）
 * <p>
 * 在 CoreApp.onCreate() 中调用：GlobalCrashHandler.install();
 */
public class GlobalCrashHandler implements Thread.UncaughtExceptionHandler {

    private static final String CRASH_DIR_NAME = "crash_logs";

    private static volatile GlobalCrashHandler sInstance;

    private GlobalCrashHandler() {
    }

    public static void install() {
        if (sInstance == null) {
            synchronized (GlobalCrashHandler.class) {
                if (sInstance == null) {
                    sInstance = new GlobalCrashHandler();
                    sInstance.init();
                }
            }
        }
    }

    private void init() {
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(@NonNull Thread thread, @NonNull Throwable ex) {
        try {
            // 1. 保存崩溃日志到文件（核心）
            File crashFile = saveCrashInfoToFile(ex);
            if (crashFile != null) {
                safeLog("App crashed, log saved to: " + crashFile.getAbsolutePath());
            } else {
                safeLog("App crashed, save log failed");
            }

            // 2. 异步上传日志（子线程，避免阻塞）
            uploadCrashLog(crashFile, ex);

            // 3. 仅在主进程显示错误页
            if (isMainProcess()) {
                showCrashDialog(crashFile);
            }

            // 4. 延迟 1 秒后杀死进程（给日志写入/上传留时间）
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                safeLog("sleep interrupted");
            }

            // 5. 杀死进程
            Process.killProcess(Process.myPid());
            System.exit(10);

        } catch (Throwable t) {
            //  二次崩溃保护
            safeLog("!!! GlobalCrashHandler crashed !!!");
            Process.killProcess(Process.myPid());
            System.exit(10);
        }
    }

    /**
     * 保存错误信息到文件中
     */
    private @Nullable File saveCrashInfoToFile(@NonNull Throwable ex) {
        Writer writer = null;
        BufferedWriter bufferedWriter = null;

        try {
            StringBuilder sb = new StringBuilder();
            appendDeviceInfo(sb);
            appendExceptionInfo(sb, ex);

            String fileName = "crash-" + getTimestamp() + ".log";
            File dir = getCrashDir();
            if (dir == null) {
                return null;
            }
            File file = new File(dir, fileName);

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
        Context context = CoreApp.getAppContext(); //  使用 CoreApp
        sb.append("======== Device Info ========\n");
        sb.append("Time: ").append(new Date()).append("\n");
        sb.append("App Version: ").append(getAppVersionName(context)).append(" (").append(getAppVersionCode(context)).append(")\n");
        sb.append("Android Version: ").append(Build.VERSION.RELEASE).append(" (").append(Build.VERSION.SDK_INT).append(")\n");
        sb.append("Device Model: ").append(safeToString(Build.MODEL)).append("\n");
        sb.append("Manufacturer: ").append(safeToString(Build.MANUFACTURER)).append("\n");
        sb.append("Brand: ").append(safeToString(Build.BRAND)).append("\n");
        sb.append("Process Name: ").append(getProcessName(context)).append("\n");
        sb.append("\n");
    }

    private void appendExceptionInfo(@NonNull StringBuilder sb, @NonNull Throwable ex) {
        sb.append("======== Exception Info ========\n");
        sb.append("Exception: ").append(safeToString(ex.toString())).append("\n\n");

        StringWriter sw = null;
        PrintWriter pw = null;
        try {
            sw = new StringWriter();
            pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            sb.append(sw);
        } finally {
            if (pw != null) pw.close();
            if (sw != null) {
                try {
                    sw.close();
                } catch (IOException e) {
                    safeLog("close StringWriter failed");
                }
            }
        }
    }

    private String getTimestamp() {
        return new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
    }

    private String getAppVersionName(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (Exception e) {
            return "unknown";
        }
    }

    private int getAppVersionCode(Context context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                return (int) context.getPackageManager().getPackageInfo(context.getPackageName(), 0).getLongVersionCode();
            } else {
                return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
            }
        } catch (Exception e) {
            return 0;
        }
    }

    private String getProcessName(Context context) {
        try {
            int pid = Process.myPid();
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
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

    /**
     * 获取崩溃日志目录（适配 Android 10+ Scoped Storage）
     */
    private @Nullable File getCrashDir() {
        try {
            Context context = CoreApp.getAppContext(); //  使用 CoreApp
            File dir = new File(context.getExternalFilesDir(null), CRASH_DIR_NAME);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    safeLog("create crash dir failed: " + dir.getAbsolutePath());
                    return null;
                }
            }
            return dir;
        } catch (Exception e) {
            safeLog("getCrashDir failed: " + e.getMessage());
            return null;
        }
    }

    /**
     * 判断是否为主进程
     */
    private boolean isMainProcess() {
        try {
            Context context = CoreApp.getAppContext(); //  使用 CoreApp
            String processName = getProcessName(context);
            String packageName = context.getPackageName();
            return TextUtils.equals(processName, packageName);
        } catch (Exception e) {
            safeLog("isMainProcess failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * 上传崩溃日志（异步，子线程执行）
     */
    private void uploadCrashLog(@Nullable final File crashFile, @NonNull final Throwable ex) {
        try {
            CrashReportUploader.uploadAsync(crashFile, ex);
        } catch (Exception e) {
            safeLog("upload crash log failed: " + e.getMessage());
        }
    }

    /**
     * 显示崩溃对话框（仅在主进程 UI 线程）
     */
    private void showCrashDialog(@Nullable final File crashFile) {
        try {
            Context context = CoreApp.getAppContext(); //  使用 CoreApp
            new android.os.Handler(context.getMainLooper()).post(() -> {
                try {
//                    Intent intent = new Intent(context, ErrorActivity.class);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                    if (crashFile != null) {
//                        intent.putExtra("crash_file", crashFile.getAbsolutePath());
//                    }
//                    context.startActivity(intent);
                } catch (Exception e) {
                    safeLog("start ErrorActivity failed: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            safeLog("showCrashDialog failed: " + e.getMessage());
        }
    }

    /**
     * 安全日志（使用 Logger，如果未初始化则 fallback 到 System.out）
     */
    private void safeLog(String msg) {
        if (msg == null) return;
        try {
            // 尝试使用 Logger
            Logger.e("[CrashHandler] %s", msg);
        } catch (Throwable t) {
            // 如果 Logger 未初始化或崩溃，fallback 到 System.out
            System.err.println("[GlobalCrashHandler Fallback] " + msg);
        }
    }

    /**
     * 安全 toString，避免空指针
     */
    private String safeToString(Object obj) {
        if (obj == null) return "null";
        try {
            return obj.toString();
        } catch (Throwable t) {
            return "toString_failed";
        }
    }
}
