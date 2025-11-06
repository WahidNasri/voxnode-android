package org.linphone.ui.web

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MenuItem
import android.graphics.Color
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.view.WindowCompat
import org.linphone.R
import org.linphone.LinphoneApplication.Companion.coreContext
import org.voxnode.voxnode.storage.VoxNodeDataManager
import androidx.core.graphics.toColorInt
import org.linphone.ui.GenericActivity

class WebViewActivity : GenericActivity() {
    companion object {
        const val EXTRA_URL = "extra_url"
        const val EXTRA_TITLE = "extra_title"
    }

    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)

        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        val titleFromIntent = intent.getStringExtra(EXTRA_TITLE)
        supportActionBar?.title = titleFromIntent ?: getString(R.string.app_name)

        // Apply provider color if available - get data on worker thread, apply on UI thread
        coreContext.postOnCoreThread {
            try {
                val loginResult = VoxNodeDataManager.getLoginResult()
                val providerColor = loginResult?.providerColor1
                if (!providerColor.isNullOrEmpty()) {
                    // Ensure color has '#' prefix
                    val colorString = if (providerColor.startsWith("#")) {
                        providerColor
                    } else {
                        "#$providerColor"
                    }
                    
                    val color = colorString.toColorInt()
                    
                    // Switch back to UI thread for UI operations
                    runOnUiThread {
                        // Apply color to toolbar (on UI thread)
                        toolbar.setBackgroundColor(color)
                        
                        // Apply color to status bar - use post to ensure it's applied after window is ready
                        window.decorView.post {
                            // Use modern approach for status bar styling
                            val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
                            
                            // Set status bar content color based on color brightness
                            windowInsetsController.isAppearanceLightStatusBars = true

                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

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

    override fun onResume() {
        super.onResume()
        
        // Reapply status bar color in case it was reset - get data on worker thread, apply on UI thread
        coreContext.postOnCoreThread {
            try {
                val loginResult = VoxNodeDataManager.getLoginResult()
                val providerColor = loginResult?.providerColor1
                if (!providerColor.isNullOrEmpty()) {
                    val colorString = if (providerColor.startsWith("#")) {
                        providerColor
                    } else {
                        "#$providerColor"
                    }
                    
                    val color = colorString.toColorInt()
                    
                    // Switch back to UI thread for UI operations
                    runOnUiThread {
                        // Use modern approach for status bar styling
                        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
                        
                        // Set status bar content color based on color brightness
                        val isLightColor = isColorLight(color)
                        windowInsetsController.isAppearanceLightStatusBars = isLightColor

                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
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

    private fun isColorLight(color: Int): Boolean {
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        
        // Calculate relative luminance
        val luminance = (0.299 * red + 0.587 * green + 0.114 * blue) / 255
        
        // If luminance is greater than 0.5, the color is light
        return luminance > 0.5
    }
}
