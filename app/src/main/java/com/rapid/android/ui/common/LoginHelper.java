package com.rapid.android.ui.common;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.rapid.android.R;
import com.rapid.android.core.data.session.SessionManager;
import com.rapid.android.core.ui.components.dialog.DialogController;
import com.rapid.android.core.ui.utils.ToastUtils;
import com.rapid.android.feature.login.LoginActivity;

/**
 * 登录状态检查工具类，提供统一的登录检查和跳转逻辑
 */
public class LoginHelper {

    /**
     * 检查登录状态，如果未登录则显示提示并跳转到登录页
     *
     * @param context          上下文
     * @param dialogController Dialog 控制器，用于显示 Toast
     * @param action           登录后执行的操作
     * @return 是否已登录
     */
    public static boolean requireLogin(@NonNull Context context,
                                       @NonNull DialogController dialogController,
                                       @Nullable Runnable action) {
        if (SessionManager.getInstance().isLoggedIn()) {
            if (action != null) {
                action.run();
            }
            return true;
        }

        ToastUtils.showShortToast(dialogController, context.getString(R.string.mine_toast_require_login));
        context.startActivity(new Intent(context, LoginActivity.class));
        return false;
    }

    /**
     * 检查登录状态，如果未登录则显示提示并跳转到登录页
     *
     * @param context          上下文
     * @param dialogController Dialog 控制器，用于显示 Toast
     * @return 是否已登录
     */
    public static boolean requireLogin(@NonNull Context context,
                                       @NonNull DialogController dialogController) {
        return requireLogin(context, dialogController, null);
    }

    /**
     * 检查登录状态，不显示提示
     *
     * @return 是否已登录
     */
    public static boolean isLoggedIn() {
        return SessionManager.getInstance().isLoggedIn();
    }
}
