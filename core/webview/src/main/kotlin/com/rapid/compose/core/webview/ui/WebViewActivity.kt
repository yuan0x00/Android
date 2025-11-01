package com.rapid.compose.core.webview.ui

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.rapid.android.core.common.utils.WindowInsetsUtils
import com.rapid.compose.core.webview.R
import com.rapid.compose.core.webview.callback.WebViewConfig

class WebViewActivity : AppCompatActivity(),
    WebViewFragment.OnWebViewTitleChangeListener,
    WebViewFragment.OnWebViewErrorListener,
    WebViewFragment.OnShouldOverrideUrlListener {

    companion object {
        const val EXTRA_TITLE = "extra_title"

        @JvmStatic
        @JvmOverloads
        fun start(
            context: Context,
            url: String,
            title: String? = null,
            config: WebViewConfig? = null
        ) {
            WebViewFragment.startActivity(context, url, title, config)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)

        WindowInsetsUtils.applySystemWindowInsets(findViewById(android.R.id.content))

        val title = intent.getStringExtra(EXTRA_TITLE)

        val toolbar = findViewById<Toolbar?>(R.id.toolbar)
        if (toolbar != null) {
            setSupportActionBar(toolbar)
            if (supportActionBar != null) {
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
                title?.let { supportActionBar?.title = it }
            }
            toolbar.setNavigationOnClickListener { finish() }
        }

        val url = intent.getStringExtra(WebViewFragment.ARG_URL)
        val config = intent.getSerializableExtra(WebViewFragment.ARG_CONFIG) as? WebViewConfig

        // 显示 WebViewFragment
        if (supportFragmentManager.findFragmentById(R.id.container) == null) {
            val fragment = WebViewFragment.newInstance(url, config)
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit()
        }
    }

    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentById(R.id.container) as? WebViewFragment
//        if (fragment?.goBack() == true) {
//            return
//        }
        super.onBackPressed()
    }

    // ============ Fragment 回调接口 ============

    override fun onTitleChanged(title: String?) {
        title?.let { setTitle(it) }
    }

    override fun onError(errorCode: Int, description: String?, failingUrl: String?) {
        // 处理错误，比如显示错误页面
    }

    override fun shouldOverrideUrl(url: String?): Boolean {
        // 处理 URL 重定向逻辑
        return false
    }
}