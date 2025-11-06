package com.rapid.android.core.analytics.tracker;

public class CommonFields {
    private String deviceId;
    private String userId;
    private String appVersion;

    public CommonFields(String deviceId, String userId, String appVersion) {
        this.deviceId = deviceId;
        this.userId = userId;
        this.appVersion = appVersion;
    }

    public String getDeviceId() { return deviceId; }
    public String getUserId() { return userId; }
    public String getAppVersion() { return appVersion; }
}
