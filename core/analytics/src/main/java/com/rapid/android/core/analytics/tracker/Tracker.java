package com.rapid.android.core.analytics.tracker;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Tracker {

    private static EventBuffer eventBuffer = new EventBuffer();
    private static CommonFields commonFields;
    private static String sessionId;

    // 初始化 SDK
    public static void init(CommonFields common) {
        commonFields = common;
        sessionId = UUID.randomUUID().toString();
        eventBuffer.start();
    }

    // 核心埋点接口
    public static void track(String eventName, Map<String, Object> params) {
        Map<String, Object> allParams = new HashMap<>();
        if (params != null) {
            allParams.putAll(params);
        }

        // 自动添加公共字段
        if (commonFields != null) {
            allParams.put("device_id", commonFields.getDeviceId());
            allParams.put("user_id", commonFields.getUserId());
            allParams.put("app_version", commonFields.getAppVersion());
        }
//        allParams.put("os_version", System.getProperty("os.version"));
        allParams.put("platform", "Android");
//        allParams.put("session_id", sessionId);

        Event event = new Event(eventName, allParams);
        eventBuffer.enqueue(event);
    }

    // 点击埋点
    public static void trackClick(String viewId) {
        Map<String, Object> params = new HashMap<>();
        params.put("view_id", viewId);
        track("click", params);
    }

    // 页面创建埋点
    public static void trackPageCreate(String pageName) {
        Map<String, Object> params = new HashMap<>();
        params.put("page", pageName);
        track("page_create", params);
    }

    //页面销毁埋点
    public static void trackPageDestroy(String pageName, long duration) {
        Map<String, Object> params = new HashMap<>();
        params.put("page", pageName);
        params.put("duration_ms", duration);
        track("page_destroy", params);
    }

    // 获取当前公共字段
    public static CommonFields getCommonFields() {
        return commonFields;
    }

    // 切换用户
    public static void setUserId(String userId) {
        if (commonFields != null) {
            commonFields = new CommonFields(commonFields.getDeviceId(), userId, commonFields.getAppVersion());
        }
    }
}
