package com.rapid.android.core.navigation;

import android.content.Intent;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

/**
 * ActivityResult API 辅助类
 * 封装新的 Activity Result API，简化使用
 */
public class ActivityResultHelper {

    private final ActivityResultLauncher<Intent> launcher;

    private ActivityResultHelper(@NonNull ActivityResultLauncher<Intent> launcher) {
        this.launcher = launcher;
    }

    /**
     * 在 Activity 中创建 Helper
     */
    public static ActivityResultHelper register(@NonNull FragmentActivity activity,
                                                  @NonNull ActivityResultCallback<ActivityResult> callback) {
        ActivityResultLauncher<Intent> launcher = activity.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                callback
        );
        return new ActivityResultHelper(launcher);
    }

    /**
     * 在 Fragment 中创建 Helper
     */
    public static ActivityResultHelper register(@NonNull Fragment fragment,
                                                  @NonNull ActivityResultCallback<ActivityResult> callback) {
        ActivityResultLauncher<Intent> launcher = fragment.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                callback
        );
        return new ActivityResultHelper(launcher);
    }

    /**
     * 简单的成功/失败回调
     */
    public static ActivityResultCallback<ActivityResult> simpleCallback(@NonNull ResultCallback callback) {
        return result -> callback.onResult(result.getResultCode(), result.getData());
    }

    /**
     * 只处理成功结果的回调
     */
    public static ActivityResultCallback<ActivityResult> successCallback(@NonNull SuccessCallback callback) {
        return result -> {
            if (result.getResultCode() == android.app.Activity.RESULT_OK) {
                callback.onSuccess(result.getData());
            }
        };
    }

    /**
     * 启动 Activity 并等待结果
     */
    public void launch(@NonNull Intent intent) {
        launcher.launch(intent);
    }

    /**
     * 使用 Router 路径启动并等待结果
     */
    public void launch(@NonNull FragmentActivity activity, @NonNull String path) {
        launch(activity, path, null);
    }

    /**
     * 使用 Router 路径启动并等待结果（带参数）
     */
    public void launch(@NonNull FragmentActivity activity, @NonNull String path, @Nullable android.os.Bundle extras) {
        Intent intent = Router.getInstance().buildIntent(activity, path, extras);
        if (intent != null) {
            launcher.launch(intent);
        }
    }

    /**
     * 结果处理回调接口
     */
    public interface ResultCallback {
        void onResult(int resultCode, @Nullable Intent data);
    }

    /**
     * 成功回调接口
     */
    public interface SuccessCallback {
        void onSuccess(@Nullable Intent data);
    }
}
