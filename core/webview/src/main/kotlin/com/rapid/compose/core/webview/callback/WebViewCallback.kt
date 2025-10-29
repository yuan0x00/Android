package com.rapid.compose.core.webview.callback

interface WebViewCallback {
    fun onPageStarted(url: String?)
    fun onPageFinished(url: String?)
    fun onReceivedError(errorCode: Int, description: String?, failingUrl: String?)
    fun onProgressChanged(progress: Int)
    fun onReceivedTitle(title: String?)
    fun shouldOverrideUrlLoading(url: String?): Boolean
}