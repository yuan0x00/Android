package com.rapid.android.init.tasks;

import android.app.Application;

import com.rapid.android.core.common.app.BaseApplication;
import com.rapid.android.core.common.app.init.AsyncTask;
import com.rapid.android.core.log.LogKit;
import com.rapid.android.core.webview.core.WebViewFactory;
import com.rapid.android.core.webview.utils.WebViewInitOptimizer;

public class WebviewTask extends AsyncTask {

    private static final String TAG = "WebviewTask";

    @Override
    public String getName() {
        return "Webview";
    }

    @Override
    public void execute() throws Exception {
        Application application = BaseApplication.getInstance();
        // WebView 全局初始化优化
        WebViewInitOptimizer.init(application);

        try {
            WebViewFactory.getInstance(application).prewarm(1);
            LogKit.d(TAG, "WebView prewarmed");
        } catch (Exception e) {
            LogKit.w(TAG, e, "WebView prewarm skipped");
        }

        // Debug 模式下启用 WebView 调试
//        if (BuildConfig.DEBUG) {
//            WebViewInitOptimizer.enableDebugMode();
//        }
    }
}
