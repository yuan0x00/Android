package com.rapid.android.core.analytics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * 事件追踪器
 * 提供常见事件的快捷记录方法
 */
public class EventTracker {

    /**
     * 点击事件
     */
    public static void click(@NonNull String elementName) {
        click(elementName, null);
    }

    /**
     * 点击事件（带额外参数）
     */
    public static void click(@NonNull String elementName, @Nullable Map<String, Object> extras) {
        Map<String, Object> params = new HashMap<>();
        params.put("element_name", elementName);
        params.put("action", "click");
        if (extras != null) {
            params.putAll(extras);
        }
        AnalyticsManager.getInstance().logEvent("element_click", params);
    }

    /**
     * 曝光事件
     */
    public static void exposure(@NonNull String elementName) {
        exposure(elementName, null);
    }

    /**
     * 曝光事件（带位置信息）
     */
    public static void exposure(@NonNull String elementName, @Nullable Integer position) {
        Map<String, Object> params = new HashMap<>();
        params.put("element_name", elementName);
        if (position != null) {
            params.put("position", position);
        }
        AnalyticsManager.getInstance().logEvent("element_exposure", params);
    }

    /**
     * 搜索事件
     */
    public static void search(@NonNull String keyword) {
        search(keyword, null);
    }

    /**
     * 搜索事件（带结果数量）
     */
    public static void search(@NonNull String keyword, @Nullable Integer resultCount) {
        Map<String, Object> params = new HashMap<>();
        params.put("keyword", keyword);
        if (resultCount != null) {
            params.put("result_count", resultCount);
        }
        AnalyticsManager.getInstance().logEvent("search", params);
    }

    /**
     * 分享事件
     */
    public static void share(@NonNull String contentType, @NonNull String platform) {
        Map<String, Object> params = new HashMap<>();
        params.put("content_type", contentType);
        params.put("platform", platform);
        AnalyticsManager.getInstance().logEvent("share", params);
    }

    /**
     * 登录事件
     */
    public static void login(@NonNull String method) {
        Map<String, Object> params = new HashMap<>();
        params.put("method", method);
        AnalyticsManager.getInstance().logEvent("login", params);
    }

    /**
     * 注册事件
     */
    public static void register(@NonNull String method) {
        Map<String, Object> params = new HashMap<>();
        params.put("method", method);
        AnalyticsManager.getInstance().logEvent("register", params);
    }

    /**
     * 购买事件
     */
    public static void purchase(@NonNull String productId, double price, @NonNull String currency) {
        Map<String, Object> params = new HashMap<>();
        params.put("product_id", productId);
        params.put("price", price);
        params.put("currency", currency);
        AnalyticsManager.getInstance().logEvent("purchase", params);
    }

    /**
     * 加入购物车
     */
    public static void addToCart(@NonNull String productId, int quantity) {
        Map<String, Object> params = new HashMap<>();
        params.put("product_id", productId);
        params.put("quantity", quantity);
        AnalyticsManager.getInstance().logEvent("add_to_cart", params);
    }

    /**
     * 视频播放
     */
    public static void videoPlay(@NonNull String videoId, @NonNull String videoTitle) {
        Map<String, Object> params = new HashMap<>();
        params.put("video_id", videoId);
        params.put("video_title", videoTitle);
        AnalyticsManager.getInstance().logEvent("video_play", params);
    }

    /**
     * 视频播放进度
     */
    public static void videoProgress(@NonNull String videoId, long position, long duration) {
        Map<String, Object> params = new HashMap<>();
        params.put("video_id", videoId);
        params.put("position_ms", position);
        params.put("duration_ms", duration);
        params.put("progress_percent", (int) ((position * 100) / duration));
        AnalyticsManager.getInstance().logEvent("video_progress", params);
    }

    /**
     * 下载事件
     */
    public static void download(@NonNull String contentType, @NonNull String contentId) {
        Map<String, Object> params = new HashMap<>();
        params.put("content_type", contentType);
        params.put("content_id", contentId);
        AnalyticsManager.getInstance().logEvent("download", params);
    }

    /**
     * 错误事件
     */
    public static void error(@NonNull String errorType, @NonNull String errorMessage) {
        Map<String, Object> params = new HashMap<>();
        params.put("error_type", errorType);
        params.put("error_message", errorMessage);
        AnalyticsManager.getInstance().logEvent("error", params);
    }
}
