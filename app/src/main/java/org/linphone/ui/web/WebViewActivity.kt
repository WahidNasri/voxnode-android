package org.linphone.ui.web

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MenuItem
import android.graphics.Color
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import org.linphone.R
import org.voxnode.voxnode.storage.VoxNodeDataManager

class WebViewActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_URL = "extra_url"
        const val EXTRA_TITLE = "extra_title"
    }

    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        val titleFromIntent = intent.getStringExtra(EXTRA_TITLE)
        supportActionBar?.title = titleFromIntent ?: getString(R.string.app_name)

        // Apply provider color if available
        try {
            val loginResult = VoxNodeDataManager.getLoginResult()
            val color = loginResult?.providerColor1
            if (!color.isNullOrEmpty()) {
                toolbar.setBackgroundColor(Color.parseColor(color))
            }
        } catch (_: Exception) { }

        webView = findViewById(R.id.web_view)
        val settings: WebSettings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.loadWithOverviewMode = true
        settings.useWideViewPort = true
        settings.builtInZoomControls = true
        settings.displayZoomControls = false

        webView.webViewClient = object : WebViewClient() {}
        webView.webChromeClient = WebChromeClient()

        val url = intent.getStringExtra(EXTRA_URL)
        if (!url.isNullOrEmpty()) {
            webView.loadUrl(url)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        if (this::webView.isInitialized && webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
