package com.rapid.android.core.navigation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 应用路由管理器
 * 统一管理页面跳转、路由拦截、参数传递
 */
public class Router {

    private static volatile Router instance;
    private final Map<String, Class<? extends Activity>> routes = new HashMap<>();
    private final List<RouteInterceptor> interceptors = new ArrayList<>();

    private Router() {
    }

    public static Router getInstance() {
        if (instance == null) {
            synchronized (Router.class) {
                if (instance == null) {
                    instance = new Router();
                }
            }
        }
        return instance;
    }

    /**
     * 注册路由
     */
    public Router register(@NonNull String path, @NonNull Class<? extends Activity> activityClass) {
        routes.put(path, activityClass);
        return this;
    }

    /**
     * 批量注册路由
     */
    public Router registerAll(@NonNull Map<String, Class<? extends Activity>> routeMap) {
        routes.putAll(routeMap);
        return this;
    }

    /**
     * 添加拦截器
     */
    public Router addInterceptor(@NonNull RouteInterceptor interceptor) {
        if (!interceptors.contains(interceptor)) {
            interceptors.add(interceptor);
        }
        return this;
    }

    /**
     * 移除拦截器
     */
    public Router removeInterceptor(@NonNull RouteInterceptor interceptor) {
        interceptors.remove(interceptor);
        return this;
    }

    /**
     * 导航到指定路径
     */
    public void navigate(@NonNull Context context, @NonNull String path) {
        navigate(context, path, null);
    }

    /**
     * 导航到指定路径（带参数）
     */
    public void navigate(@NonNull Context context, @NonNull String path, @Nullable Bundle extras) {
        navigate(context, path, extras, -1);
    }

    /**
     * 导航到指定路径（带参数和请求码）
     */
    public void navigate(@NonNull Context context, @NonNull String path, @Nullable Bundle extras, int requestCode) {
        // 执行拦截器
        for (RouteInterceptor interceptor : interceptors) {
            if (!interceptor.intercept(path, extras)) {
                // 拦截器返回 false，中止导航
                return;
            }
        }

        // 查找目标 Activity
        Class<? extends Activity> activityClass = routes.get(path);
        if (activityClass == null) {
            // 路径未注册，尝试作为 URL 处理
            navigateToUrl(context, path);
            return;
        }

        // 创建 Intent
        Intent intent = new Intent(context, activityClass);
        if (extras != null) {
            intent.putExtras(extras);
        }

        // 如果不是 Activity 上下文，添加 FLAG_ACTIVITY_NEW_TASK
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        // 启动 Activity
        if (requestCode >= 0 && context instanceof Activity) {
            ((Activity) context).startActivityForResult(intent, requestCode);
        } else {
            context.startActivity(intent);
        }
    }

    /**
     * 使用浏览器打开 URL
     */
    public void navigateToUrl(@NonNull Context context, @NonNull String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            if (!(context instanceof Activity)) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            context.startActivity(intent);
        } catch (Exception e) {
            // URL 格式错误或无法处理
        }
    }

    /**
     * 构建 Intent（不启动）
     */
    @Nullable
    public Intent buildIntent(@NonNull Context context, @NonNull String path, @Nullable Bundle extras) {
        Class<? extends Activity> activityClass = routes.get(path);
        if (activityClass == null) {
            return null;
        }

        Intent intent = new Intent(context, activityClass);
        if (extras != null) {
            intent.putExtras(extras);
        }
        return intent;
    }

    /**
     * 检查路径是否已注册
     */
    public boolean hasRoute(@NonNull String path) {
        return routes.containsKey(path);
    }

    /**
     * 清空所有路由
     */
    public void clear() {
        routes.clear();
        interceptors.clear();
    }

    /**
     * Builder 模式构建导航参数
     */
    public static class Builder {
        private final Context context;
        private final String path;
        private Bundle extras;
        private int requestCode = -1;

        public Builder(@NonNull Context context, @NonNull String path) {
            this.context = context;
            this.path = path;
        }

        public Builder with(@NonNull String key, @Nullable String value) {
            if (extras == null) {
                extras = new Bundle();
            }
            extras.putString(key, value);
            return this;
        }

        public Builder with(@NonNull String key, int value) {
            if (extras == null) {
                extras = new Bundle();
            }
            extras.putInt(key, value);
            return this;
        }

        public Builder with(@NonNull String key, long value) {
            if (extras == null) {
                extras = new Bundle();
            }
            extras.putLong(key, value);
            return this;
        }

        public Builder with(@NonNull String key, boolean value) {
            if (extras == null) {
                extras = new Bundle();
            }
            extras.putBoolean(key, value);
            return this;
        }

        public Builder withBundle(@NonNull Bundle bundle) {
            if (extras == null) {
                extras = new Bundle();
            }
            extras.putAll(bundle);
            return this;
        }

        public Builder requestCode(int requestCode) {
            this.requestCode = requestCode;
            return this;
        }

        public void navigate() {
            Router.getInstance().navigate(context, path, extras, requestCode);
        }
    }
}
