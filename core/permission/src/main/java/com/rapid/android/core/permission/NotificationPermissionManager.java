package com.rapid.android.core.permission;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;

/**
 * 通知权限管理器
 * 专门处理通知权限相关逻辑
 */
public final class NotificationPermissionManager {

    public static final int REQUEST_CODE_NOTIFICATION = 1001;

    private NotificationPermissionManager() {
        // 工具类，禁止实例化
    }

    /**
     * 检查通知权限是否已授予
     *
     * @param context Context
     * @return true 如果通知权限已授予
     */
    public static boolean isGranted(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ 需要检查 POST_NOTIFICATIONS 权限
            return PermissionManager.isGranted(context, Manifest.permission.POST_NOTIFICATIONS);
        } else {
            // Android 13 以下通过 NotificationManagerCompat 检查
            return NotificationManagerCompat.from(context).areNotificationsEnabled();
        }
    }

    /**
     * 请求通知权限（仅适用于 Android 13+）
     *
     * @param activity Activity
     * @return true 如果发起了权限请求，false 如果不需要请求或已授予
     */
    public static boolean request(@NonNull Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!isGranted(activity)) {
                return PermissionManager.request(activity, REQUEST_CODE_NOTIFICATION,
                        Manifest.permission.POST_NOTIFICATIONS);
            }
        }
        return false;
    }

    /**
     * 是否应该显示权限说明
     *
     * @param activity Activity
     * @return true 如果应该显示说明
     */
    public static boolean shouldShowRationale(@NonNull Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return PermissionManager.shouldShowRationale(activity, Manifest.permission.POST_NOTIFICATIONS);
        }
        return false;
    }

    /**
     * 用户是否选择了"不再询问"
     *
     * @param activity Activity
     * @return true 如果用户选择了"不再询问"
     */
    public static boolean isNeverAskAgain(@NonNull Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return PermissionManager.isNeverAskAgain(activity, Manifest.permission.POST_NOTIFICATIONS);
        }
        return false;
    }

    /**
     * 打开通知设置页面
     *
     * @param context Context
     */
    public static void openSettings(@NonNull Context context) {
        Intent intent = new Intent();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
        } else {
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * 处理权限请求结果
     *
     * @param requestCode  请求码
     * @param permissions  权限列表
     * @param grantResults 授权结果
     * @param callback     结果回调
     * @return true 如果是通知权限请求
     */
    public static boolean handleResult(int requestCode, @NonNull String[] permissions,
                                        @NonNull int[] grantResults,
                                        @NonNull NotificationPermissionCallback callback) {
        if (requestCode != REQUEST_CODE_NOTIFICATION) {
            return false;
        }

        PermissionManager.handleResult(requestCode, permissions, grantResults,
                new PermissionManager.PermissionCallback() {
                    @Override
                    public void onGranted(String[] permissions) {
                        callback.onGranted();
                    }

                    @Override
                    public void onDenied(String[] permissions) {
                        callback.onDenied();
                    }
                });

        return true;
    }

    /**
     * 通知权限回调接口
     */
    public interface NotificationPermissionCallback {
        void onGranted();

        void onDenied();
    }
}
