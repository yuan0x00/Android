package com.rapid.compose.core.webview.core

import android.content.Context
import android.util.Log
import android.view.ViewGroup
import android.webkit.WebView
import com.rapid.compose.core.webview.callback.WebViewCallback
import com.rapid.compose.core.webview.callback.WebViewConfig
import com.rapid.compose.core.webview.client.CustomWebChromeClient
import com.rapid.compose.core.webview.client.CustomWebViewClient

class WebViewManager private constructor() {

    companion object {
        const val TAG = "WebViewManager"

        @Volatile
        private var instance: WebViewManager? = null

        @JvmStatic
        fun getInstance(): WebViewManager {
            val startTime = System.currentTimeMillis()
            return instance ?: synchronized(this) {
                instance ?: WebViewManager().also {
                    instance = it
                    val cost = System.currentTimeMillis() - startTime
                    Log.d(TAG, "WebViewManager init cost: ${cost}ms")
                }
            }
        }
    }

    private val webViewPool = WebViewPool.getInstance()

    /**
     * 初始化池化配置
     */
    @JvmOverloads
    fun initPool(context: Context, config: WebViewPool.PoolConfig = WebViewPool.PoolConfig()) {
        val startTime = System.currentTimeMillis()
        webViewPool.init(context, config)
        val cost = System.currentTimeMillis() - startTime
        Log.d(TAG, "WebViewPool init cost: ${cost}ms")
    }

    /**
     * 创建 WebView 实例 - 统一从池中获取
     */
    fun createWebView(
        context: Context,
        config: WebViewConfig = WebViewConfig(),
        callback: WebViewCallback? = null
    ): WebView {
        val startTime = System.currentTimeMillis()
        val webView = webViewPool.acquireWebView(context) ?: createNewWebView(context, config)

        // 应用配置
        applyConfig(webView, config)

        // 设置回调
        if (callback != null) {
            webView.webViewClient = CustomWebViewClient(callback)
            webView.webChromeClient = CustomWebChromeClient(callback)
        }

        val cost = System.currentTimeMillis() - startTime
        Log.d(TAG, "createWebView cost: ${cost}ms")

        return webView
    }

    /**
     * 释放 WebView 实例 - 统一释放回池
     */
    fun releaseWebView(webView: WebView) {
        try {
            // 立即停止所有活动
            webView.stopLoading()

            // 立即断开所有回调
            webView.webViewClient = CustomWebViewClient()
            webView.webChromeClient = CustomWebChromeClient()

            // 使用池的释放机制
            (webView.parent as? ViewGroup)?.removeView(webView)
            webViewPool.releaseWebView(webView)

        } catch (_: Exception) {
            // 如果出现异常，直接销毁
            webView.destroy()
        }
    }

    /**
     * 获取池状态
     */
    fun getPoolStatus() = webViewPool.getPoolStatus()

    /**
     * 清理所有资源
     */
    fun clear() {
        webViewPool.clear()
    }

    private fun createNewWebView(context: Context, config: WebViewConfig): WebView {
        return WebView(context.applicationContext).apply {
            applyConfig(this, config)
        }
    }

    private fun applyConfig(webView: WebView, config: WebViewConfig) {
        val settings = webView.settings

        // 基础设置
        settings.javaScriptEnabled = config.javaScriptEnabled
        settings.domStorageEnabled = config.domStorageEnabled
        settings.databaseEnabled = config.databaseEnabled
        settings.setSupportZoom(config.supportZoom)
        settings.builtInZoomControls = config.builtInZoomControls
        settings.displayZoomControls = config.displayZoomControls

        // 缓存设置
        settings.cacheMode = config.cacheMode

        // 安全设置
        settings.allowFileAccess = config.allowFileAccess
        settings.allowContentAccess = config.allowContentAccess

        // 视图设置
        settings.loadWithOverviewMode = config.loadWithOverviewMode
        settings.useWideViewPort = config.useWideViewPort

        // 用户代理
        config.userAgent?.let { settings.userAgentString = it }
    }
}