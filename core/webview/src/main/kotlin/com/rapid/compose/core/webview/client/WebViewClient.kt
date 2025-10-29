package com.rapid.compose.core.webview.client

import android.graphics.Bitmap
import android.webkit.WebView
import android.webkit.WebViewClient
import com.rapid.compose.core.webview.callback.WebViewCallback
import java.lang.ref.WeakReference

class CustomWebViewClient(
    callback: WebViewCallback? = null
) : WebViewClient() {

    private val weakCallback = WeakReference(callback)

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        weakCallback.get()?.onPageStarted(url)
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        weakCallback.get()?.onPageFinished(url)
    }

    override fun onReceivedError(
        view: WebView?,
        errorCode: Int,
        description: String?,
        failingUrl: String?
    ) {
        weakCallback.get()?.onReceivedError(errorCode, description, failingUrl)
    }

    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        return weakCallback.get()?.shouldOverrideUrlLoading(url) ?: false
    }
}