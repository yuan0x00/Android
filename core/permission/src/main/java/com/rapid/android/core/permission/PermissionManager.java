package com.rapid.android.core.permission;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * 通用权限管理器
 * 提供统一的权限请求和检查接口
 */
public final class PermissionManager {

    private PermissionManager() {
        // 工具类，禁止实例化
    }

    /**
     * 检查权限是否已授予
     *
     * @param context    Context
     * @param permission 权限名称
     * @return true 如果权限已授予
     */
    public static boolean isGranted(@NonNull Context context, @NonNull String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * 检查多个权限是否全部授予
     *
     * @param context     Context
     * @param permissions 权限列表
     * @return true 如果所有权限都已授予
     */
    public static boolean areAllGranted(@NonNull Context context, @NonNull String... permissions) {
        for (String permission : permissions) {
            if (!isGranted(context, permission)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 请求权限
     *
     * @param activity    Activity
     * @param requestCode 请求码
     * @param permissions 权限列表
     * @return true 如果发起了权限请求，false 如果权限已授予或无需请求
     */
    public static boolean request(@NonNull Activity activity, int requestCode, @NonNull String... permissions) {
        // 过滤未授予的权限
        String[] deniedPermissions = filterDeniedPermissions(activity, permissions);

        if (deniedPermissions.length == 0) {
            // 所有权限已授予
            return false;
        }

        // 请求权限
        ActivityCompat.requestPermissions(activity, deniedPermissions, requestCode);
        return true;
    }

    /**
     * 是否应该显示权限说明
     *
     * @param activity   Activity
     * @param permission 权限名称
     * @return true 如果应该显示说明
     */
    public static boolean shouldShowRationale(@NonNull Activity activity, @NonNull String permission) {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
    }

    /**
     * 检查用户是否选择了"不再询问"
     *
     * @param activity   Activity
     * @param permission 权限名称
     * @return true 如果用户选择了"不再询问"
     */
    public static boolean isNeverAskAgain(@NonNull Activity activity, @NonNull String permission) {
        return !isGranted(activity, permission) && !shouldShowRationale(activity, permission);
    }

    /**
     * 处理权限请求结果
     *
     * @param requestCode  请求码
     * @param permissions  权限列表
     * @param grantResults 授权结果
     * @param callback     结果回调
     */
    public static void handleResult(int requestCode, @NonNull String[] permissions,
                                     @NonNull int[] grantResults,
                                     @NonNull PermissionCallback callback) {
        if (grantResults.length == 0) {
            callback.onDenied(permissions);
            return;
        }

        boolean allGranted = true;
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }

        if (allGranted) {
            callback.onGranted(permissions);
        } else {
            callback.onDenied(permissions);
        }
    }

    /**
     * 打开应用设置页面
     *
     * @param context Context
     */
    public static void openAppSettings(@NonNull Context context) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + context.getPackageName()));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * 过滤未授予的权限
     */
    private static String[] filterDeniedPermissions(@NonNull Context context, @NonNull String[] permissions) {
        java.util.List<String> denied = new java.util.ArrayList<>();
        for (String permission : permissions) {
            if (!isGranted(context, permission)) {
                denied.add(permission);
            }
        }
        return denied.toArray(new String[0]);
    }

    /**
     * 权限请求回调接口
     */
    public interface PermissionCallback {
        /**
         * 权限已授予
         */
        void onGranted(String[] permissions);

        /**
         * 权限被拒绝
         */
        void onDenied(String[] permissions);
    }
}
