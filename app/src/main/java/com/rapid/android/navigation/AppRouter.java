package com.rapid.android.navigation;

import com.rapid.android.core.navigation.Router;
import com.rapid.android.feature.search.SearchActivity;
import com.rapid.android.feature.setting.SettingActivity;

/**
 * 应用路由配置
 * 定义所有页面的路由路径
 */
public final class AppRouter {

    // 路由路径常量
    public static final String PATH_SEARCH = "search";
    public static final String PATH_SETTINGS = "settings";
    public static final String PATH_WEBVIEW = "webview";

    // 参数键常量
    public static final String PARAM_URL = "url";
    public static final String PARAM_TITLE = "title";

    private AppRouter() {
    }

    /**
     * 初始化路由表
     */
    public static void init() {
        Router.getInstance()
                .register(PATH_SEARCH, SearchActivity.class)
                .register(PATH_SETTINGS, SettingActivity.class);
    }
}
