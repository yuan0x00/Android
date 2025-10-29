package com.rapid.compose.core.webview.client

import android.webkit.WebChromeClient
import android.webkit.WebView
import com.rapid.compose.core.webview.callback.WebViewCallback

class CustomWebChromeClient(
    private val callback: WebViewCallback? = null
) : WebChromeClient() {

    override fun onProgressChanged(view: WebView?, newProgress: Int) {
        super.onProgressChanged(view, newProgress)
        callback?.onProgressChanged(newProgress)
    }

    override fun onReceivedTitle(view: WebView?, title: String?) {
        super.onReceivedTitle(view, title)
        callback?.onReceivedTitle(title)
    }
}