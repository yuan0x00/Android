package com.rapid.compose.core.webview.core

import android.content.Context
import android.view.ViewGroup
import android.webkit.WebView
import com.rapid.compose.core.webview.client.CustomWebChromeClient
import com.rapid.compose.core.webview.client.CustomWebViewClient
import java.util.concurrent.ConcurrentLinkedQueue

class WebViewPool private constructor() {

    companion object {
        @Volatile
        private var instance: WebViewPool? = null

        fun getInstance(): WebViewPool {
            return instance ?: synchronized(this) {
                instance ?: WebViewPool().also { instance = it }
            }
        }
    }

    private val availableWebViews = ConcurrentLinkedQueue<WebView>()
    private val inUseWebViews = mutableSetOf<WebView>()
    private var isInitialized = false

    private var maxPoolSize = 3
    private var minPoolSize = 1

    data class PoolConfig(
        val maxSize: Int = 3,
        val minSize: Int = 1
    )

    fun init(context: Context, config: PoolConfig = PoolConfig()) {
        if (isInitialized) return

        maxPoolSize = config.maxSize
        minPoolSize = config.minSize

        // 预创建最小数量的 WebView
        repeat(minPoolSize) {
            availableWebViews.offer(createWebView(context))
        }

        isInitialized = true
    }

    fun acquireWebView(context: Context): WebView? {
        if (!isInitialized) {
            init(context)
        }

        synchronized(inUseWebViews) {
            var webView = availableWebViews.poll()

            if (webView == null && inUseWebViews.size < maxPoolSize) {
                webView = createWebView(context)
            }

            webView?.let {
                inUseWebViews.add(it)
                resetWebView(it)
            }

            return webView
        }
    }

    fun releaseWebView(webView: WebView) {
        synchronized(inUseWebViews) {
            // 确保从 inUseWebViews 中移除
            if (inUseWebViews.remove(webView)) {
                if (availableWebViews.size < maxPoolSize) {
                    resetWebView(webView)
                    availableWebViews.offer(webView)
                } else {
                    webView.destroy()
                }
            }
        }
    }

    fun getPoolStatus(): PoolStatus {
        synchronized(inUseWebViews) {
            return PoolStatus(
                availableCount = availableWebViews.size,
                inUseCount = inUseWebViews.size,
                totalCount = totalSize(),
                maxPoolSize = maxPoolSize,
                minPoolSize = minPoolSize
            )
        }
    }

    fun clear() {
        synchronized(inUseWebViews) {
            availableWebViews.forEach { it.destroy() }
            availableWebViews.clear()
            inUseWebViews.clear()
            isInitialized = false
        }
    }

    private fun createWebView(context: Context): WebView {
        return WebView(context.applicationContext)
    }

    private fun resetWebView(webView: WebView) {
        // 1. 停止加载（双重保险）
        webView.stopLoading()

        // 2. 清理所有回调（关键！）
        webView.webViewClient = CustomWebViewClient()
        webView.webChromeClient = CustomWebChromeClient()
        webView.webViewRenderProcessClient = null

        // 3. 清理所有监听器
        webView.setOnTouchListener(null)
        webView.setOnKeyListener(null)
        webView.setOnLongClickListener(null)

        // 4. 清除历史
        webView.clearHistory()

        // 5. 加载空白页，取消任何 pending 的请求
        webView.loadUrl("about:blank")

        // 6. 从父布局移除
        (webView.parent as? ViewGroup)?.removeView(webView)

        // 7. 清除 WebView 内部状态
        try {
            webView.clearCache(true)
        } catch (_: Exception) {
            // 忽略清理时的异常
        }
    }

    private fun totalSize(): Int {
        return availableWebViews.size + inUseWebViews.size
    }

    data class PoolStatus(
        val availableCount: Int,
        val inUseCount: Int,
        val totalCount: Int,
        val maxPoolSize: Int,
        val minPoolSize: Int
    )
}