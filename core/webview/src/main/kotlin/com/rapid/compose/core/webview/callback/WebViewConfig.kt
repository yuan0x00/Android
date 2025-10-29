package com.rapid.compose.core.webview.callback

import android.webkit.WebSettings
import java.io.Serializable

data class WebViewConfig(
    // 基础配置
    val javaScriptEnabled: Boolean = true,
    val domStorageEnabled: Boolean = true,
    val databaseEnabled: Boolean = true,
    val supportZoom: Boolean = true,
    val builtInZoomControls: Boolean = true,
    val displayZoomControls: Boolean = false,

    // 缓存配置
    val cacheMode: Int = WebSettings.LOAD_DEFAULT,
    val setAppCacheEnabled: Boolean = true,

    // 安全配置
    val allowFileAccess: Boolean = false,
    val allowContentAccess: Boolean = false,

    // 视图配置
    val loadWithOverviewMode: Boolean = true,
    val useWideViewPort: Boolean = true,

    // 用户代理
    val userAgent: String? = null,

    // 池化配置
    val usePool: Boolean = true,
    val preloadKey: String? = null
) : Serializable