package com.core.webview.permission;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.webkit.PermissionRequest;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * WebView权限管理器
 * 处理WebView中的各种权限请求（如摄像头、麦克风、地理位置等）
 */
public class PermissionManager {
    private static final String TAG = "WebViewPermission";

    private final Context context;
    private final Map<String, PermissionHandler> permissionHandlers = new HashMap<>();
    private final List<PermissionRequest> pendingRequests = new ArrayList<>();

    private PermissionListener listener;
    private boolean autoGrantSafePermissions = true;

    public PermissionManager(@NonNull Context context) {
        this.context = context;
        initializeDefaultHandlers();
    }

    /**
     * 设置权限监听器
     */
    public PermissionManager setPermissionListener(PermissionListener listener) {
        this.listener = listener;
        return this;
    }

    /**
     * 设置是否自动授予安全权限
     */
    public PermissionManager setAutoGrantSafePermissions(boolean autoGrant) {
        this.autoGrantSafePermissions = autoGrant;
        return this;
    }

    /**
     * 注册权限处理器
     */
    public PermissionManager registerHandler(@NonNull String resource, @NonNull PermissionHandler handler) {
        permissionHandlers.put(resource, handler);
        return this;
    }

    /**
     * 处理权限请求
     */
    public void handlePermissionRequest(@NonNull PermissionRequest request) {
        Uri origin = request.getOrigin();
        String[] resources = request.getResources();

        Log.d(TAG, "Permission request from " + origin + " for resources: " + String.join(", ", resources));

        List<String> grantedResources = new ArrayList<>();
        List<String> deniedResources = new ArrayList<>();

        for (String resource : resources) {
            boolean granted = checkAndRequestPermission(origin, resource);
            if (granted) {
                grantedResources.add(resource);
            } else {
                deniedResources.add(resource);
            }
        }

        // 授予权限
        if (!grantedResources.isEmpty()) {
            String[] grantedArray = grantedResources.toArray(new String[0]);
            request.grant(grantedArray);
            Log.d(TAG, "Granted permissions: " + String.join(", ", grantedResources));
        }

        // 拒绝权限
        if (!deniedResources.isEmpty()) {
            request.deny();
            Log.d(TAG, "Denied permissions: " + String.join(", ", deniedResources));
        }

        // 通知监听器
        if (listener != null) {
            boolean allGranted = deniedResources.isEmpty();
            listener.onPermissionRequested(origin, resources, allGranted);
        }
    }

    /**
     * 检查并请求权限
     */
    private boolean checkAndRequestPermission(Uri origin, String resource) {
        // 使用自定义处理器
        PermissionHandler handler = permissionHandlers.get(resource);
        if (handler != null) {
            return handler.handlePermission(origin, resource);
        }

        // 默认处理逻辑
        switch (resource) {
            case PermissionRequest.RESOURCE_AUDIO_CAPTURE:
                return checkMicrophonePermission();

            case PermissionRequest.RESOURCE_VIDEO_CAPTURE:
                return checkCameraPermission();

            case PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID:
                return autoGrantSafePermissions; // DRM相关，比较安全

            case PermissionRequest.RESOURCE_MIDI_SYSEX:
                return autoGrantSafePermissions; // MIDI系统独占，比较安全

            default:
                Log.w(TAG, "Unknown permission resource: " + resource);
                return false;
        }
    }

    /**
     * 检查麦克风权限
     */
    private boolean checkMicrophonePermission() {
        return checkAndroidPermission(Manifest.permission.RECORD_AUDIO);
    }

    /**
     * 检查相机权限
     */
    private boolean checkCameraPermission() {
        return checkAndroidPermission(Manifest.permission.CAMERA);
    }

    /**
     * 检查Android权限
     */
    private boolean checkAndroidPermission(String permission) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true; // 旧版本自动授予
        }

        int result = ContextCompat.checkSelfPermission(context, permission);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        }

        // 如果是Activity，请求权限
        if (context instanceof Activity) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{permission}, 100);
        }

        return false;
    }

    /**
     * 初始化默认处理器
     */
    private void initializeDefaultHandlers() {
        // 地理位置权限处理器
        registerHandler("geolocation", (origin, resource) -> {
            return checkAndroidPermission(Manifest.permission.ACCESS_FINE_LOCATION) ||
                   checkAndroidPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
        });

        // 通知权限处理器
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerHandler("notifications", (origin, resource) ->
                checkAndroidPermission(Manifest.permission.POST_NOTIFICATIONS));
        }
    }

    /**
     * 撤销权限
     */
    public void revokePermissions(@NonNull Uri origin, @NonNull String[] resources) {
        Log.d(TAG, "Revoking permissions from " + origin + ": " + String.join(", ", resources));

        if (listener != null) {
            listener.onPermissionRevoked(origin, resources);
        }
    }

    /**
     * 获取待处理的权限请求数量
     */
    public int getPendingRequestCount() {
        return pendingRequests.size();
    }

    /**
     * 清理资源
     */
    public void cleanup() {
        permissionHandlers.clear();
        pendingRequests.clear();
    }

    public interface PermissionListener {
        /**
         * 权限请求
         * @param origin 请求源
         * @param resources 请求的权限资源
         * @param granted 是否已授予
         */
        void onPermissionRequested(@NonNull Uri origin, @NonNull String[] resources, boolean granted);

        /**
         * 权限被撤销
         * @param origin 请求源
         * @param resources 被撤销的权限
         */
        void onPermissionRevoked(@NonNull Uri origin, @NonNull String[] resources);
    }

    public interface PermissionHandler {
        /**
         * 处理权限请求
         * @param origin 请求源
         * @param resource 请求的权限资源
         * @return 是否授予权限
         */
        boolean handlePermission(@NonNull Uri origin, @NonNull String resource);
    }
}
