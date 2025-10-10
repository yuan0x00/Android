package com.rapid.android.core.navigation;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Deep Link 处理器
 * 统一处理应用的 Deep Link 和 App Link
 */
public class DeepLinkHandler {

    private static volatile DeepLinkHandler instance;
    private final Map<String, DeepLinkParser> parsers = new HashMap<>();

    private DeepLinkHandler() {
    }

    public static DeepLinkHandler getInstance() {
        if (instance == null) {
            synchronized (DeepLinkHandler.class) {
                if (instance == null) {
                    instance = new DeepLinkHandler();
                }
            }
        }
        return instance;
    }

    /**
     * 注册 Deep Link 解析器
     */
    public DeepLinkHandler register(@NonNull String scheme, @NonNull DeepLinkParser parser) {
        parsers.put(scheme, parser);
        return this;
    }

    /**
     * 处理 Deep Link
     *
     * @return true 如果成功处理，false 如果无法处理
     */
    public boolean handle(@NonNull Context context, @Nullable Intent intent) {
        if (intent == null) {
            return false;
        }

        Uri uri = intent.getData();
        if (uri == null) {
            return false;
        }

        return handle(context, uri);
    }

    /**
     * 处理 Deep Link URI
     */
    public boolean handle(@NonNull Context context, @NonNull Uri uri) {
        String scheme = uri.getScheme();
        if (TextUtils.isEmpty(scheme)) {
            return false;
        }

        // 查找对应的解析器
        DeepLinkParser parser = parsers.get(scheme);
        if (parser == null) {
            return false;
        }

        // 解析 URI
        DeepLinkResult result = parser.parse(uri);
        if (result == null || TextUtils.isEmpty(result.path)) {
            return false;
        }

        // 执行导航
        Router.getInstance().navigate(context, result.path, result.extras);
        return true;
    }

    /**
     * Deep Link 解析器接口
     */
    public interface DeepLinkParser {
        @Nullable
        DeepLinkResult parse(@NonNull Uri uri);
    }

    /**
     * Deep Link 解析结果
     */
    public static class DeepLinkResult {
        public final String path;
        public final Bundle extras;

        public DeepLinkResult(@NonNull String path) {
            this(path, null);
        }

        public DeepLinkResult(@NonNull String path, @Nullable Bundle extras) {
            this.path = path;
            this.extras = extras;
        }
    }

    /**
     * 默认的 Deep Link 解析器
     * 格式: scheme://host/path?param1=value1&param2=value2
     */
    public static class DefaultDeepLinkParser implements DeepLinkParser {

        private final String host;

        public DefaultDeepLinkParser(@NonNull String host) {
            this.host = host;
        }

        @Nullable
        @Override
        public DeepLinkResult parse(@NonNull Uri uri) {
            // 检查 host 是否匹配
            if (!host.equals(uri.getHost())) {
                return null;
            }

            // 获取路径
            String path = uri.getPath();
            if (TextUtils.isEmpty(path)) {
                return null;
            }

            // 去掉开头的 '/'
            if (path.startsWith("/")) {
                path = path.substring(1);
            }

            // 解析查询参数
            Bundle extras = new Bundle();
            Set<String> paramNames = uri.getQueryParameterNames();
            for (String paramName : paramNames) {
                String paramValue = uri.getQueryParameter(paramName);
                if (paramValue != null) {
                    extras.putString(paramName, paramValue);
                }
            }

            return new DeepLinkResult(path, extras.isEmpty() ? null : extras);
        }
    }
}
