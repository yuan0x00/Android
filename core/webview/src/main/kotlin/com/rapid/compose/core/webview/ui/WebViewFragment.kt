package com.rapid.compose.core.webview.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import com.rapid.compose.core.webview.R
import com.rapid.compose.core.webview.callback.WebViewCallback
import com.rapid.compose.core.webview.callback.WebViewConfig
import com.rapid.compose.core.webview.core.WebViewManager

class WebViewFragment : Fragment(), WebViewCallback {

    companion object {
        const val ARG_URL = "arg_url"
        const val ARG_CONFIG = "arg_config"

        fun newInstance(
            url: String? = null,
            config: WebViewConfig? = null
        ): WebViewFragment {
            return WebViewFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_URL, url)
                    putSerializable(ARG_CONFIG, config)
                }
            }
        }

        fun startActivity(
            context: Context,
            url: String,
            title: String? = null,
            config: WebViewConfig? = null
        ) {
            val intent = Intent(context, WebViewActivity::class.java).apply {
                putExtra(ARG_URL, url)
                putExtra(WebViewActivity.EXTRA_TITLE, title)
                putExtra(ARG_CONFIG, config)
            }
            context.startActivity(intent)
        }
    }

    private var webView: WebView? = null
    private lateinit var progressBar: ProgressBar
    private lateinit var webViewContainer: ViewGroup

    private var url: String? = null
    private var config: WebViewConfig = WebViewConfig()
    private val webViewManager = WebViewManager.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_webview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        parseArguments()
        initViews(view)
        setupWebView()
    }

    private fun parseArguments() {
        arguments?.let {
            url = it.getString(ARG_URL)
            config = (it.getSerializable(ARG_CONFIG) as? WebViewConfig) ?: WebViewConfig()
        }
    }

    private fun initViews(view: View) {
        progressBar = view.findViewById(R.id.progressBar)
        webViewContainer = view.findViewById(R.id.webViewContainer)

        // 从池中获取 WebView 实例
        webView = webViewManager.createWebView(requireContext(), config, this)

        // 添加到布局
        webView?.let { wv ->
            webViewContainer.addView(wv)
        }
    }

    private fun setupWebView() {
        url?.let { webView?.loadUrl(it) }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        webView?.let { wv ->
            // 立即停止所有活动
            wv.stopLoading()
            wv.webChromeClient = null

            // 确保释放逻辑执行
            ensureWebViewRelease(wv)

            webView = null
        }
    }

    private fun ensureWebViewRelease(webView: WebView) {
        // 方法1：立即执行（如果可能）
        if (webViewContainer.isAttachedToWindow) {
            webViewContainer.post {
                releaseWebViewImmediately(webView)
            }
        } else {
            // 方法2：直接执行（如果容器已销毁）
            releaseWebViewImmediately(webView)
        }
    }

    private fun releaseWebViewImmediately(webView: WebView) {
        // 从父布局移除
        (webView.parent as? ViewGroup)?.removeView(webView)
        // 释放回池
        webViewManager.releaseWebView(webView)
    }

    override fun onDestroy() {
        super.onDestroy()
        // 最终清理
        webView?.let { wv ->
            releaseWebViewImmediately(wv)
            webView = null
        }
    }
    // ============ 公共 WebView 操作方法 ============

    fun loadUrl(url: String) {
        this.url = url
        webView?.loadUrl(url)
    }

    fun loadData(data: String, mimeType: String = "text/html", encoding: String = "UTF-8") {
        webView?.loadData(data, mimeType, encoding)
    }

    fun loadDataWithBaseURL(
        baseUrl: String?,
        data: String,
        mimeType: String = "text/html",
        encoding: String = "UTF-8",
        historyUrl: String? = null
    ) {
        webView?.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl)
    }

    fun goBack(): Boolean {
        return if (webView?.canGoBack() == true) {
            webView?.goBack()
            true
        } else {
            false
        }
    }

    fun goForward(): Boolean {
        return if (webView?.canGoForward() == true) {
            webView?.goForward()
            true
        } else {
            false
        }
    }

    fun reload() {
        webView?.reload()
    }

    fun stopLoading() {
        webView?.stopLoading()
    }

    fun canGoBack(): Boolean = webView?.canGoBack() ?: false

    fun canGoForward(): Boolean = webView?.canGoForward() ?: false

    fun evaluateJavascript(script: String, resultCallback: ((String) -> Unit)? = null) {
        webView?.evaluateJavascript(script) { result ->
            resultCallback?.invoke(result ?: "")
        }
    }

    fun clearCache() {
        webView?.clearCache(true)
    }

    fun clearHistory() {
        webView?.clearHistory()
    }

    // ============ WebViewCallback 实现 ============

    override fun onPageStarted(url: String?) {
        progressBar.visibility = View.VISIBLE
    }

    override fun onPageFinished(url: String?) {
        progressBar.visibility = View.GONE
    }

    override fun onProgressChanged(progress: Int) {
        progressBar.progress = progress
        if (progress == 100) {
            progressBar.visibility = View.GONE
        }
    }

    override fun onReceivedTitle(title: String?) {
        (activity as? OnWebViewTitleChangeListener)?.onTitleChanged(title)
    }

    override fun onReceivedError(errorCode: Int, description: String?, failingUrl: String?) {
        progressBar.visibility = View.GONE
        (activity as? OnWebViewErrorListener)?.onError(errorCode, description, failingUrl)
    }

    override fun shouldOverrideUrlLoading(url: String?): Boolean {
        return (activity as? OnShouldOverrideUrlListener)?.shouldOverrideUrl(url) ?: false
    }

    interface OnWebViewTitleChangeListener {
        fun onTitleChanged(title: String?)
    }

    interface OnWebViewErrorListener {
        fun onError(errorCode: Int, description: String?, failingUrl: String?)
    }

    interface OnShouldOverrideUrlListener {
        fun shouldOverrideUrl(url: String?): Boolean
    }
}