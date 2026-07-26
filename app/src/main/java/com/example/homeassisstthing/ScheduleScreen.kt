package com.example.homeassisstthing

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Bitmap
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.CookieManager
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ScheduleScreen(
    haIpAddress: String,
    schedulePath: String,
    currentTextColor: Color = Color.White,
    currentBgColor: Color = Color(0xFF0D1117),
    neonCyan: Color = Color(0xFF00FFFF),
    onBackClick: () -> Unit
) {
    val view = LocalView.current
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }

    // System immersive mode configuration
    DisposableEffect(Unit) {
        val window = (view.context as? Activity)?.window
        window?.let { win ->
            val controller = WindowCompat.getInsetsController(win, view)
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.systemBars())
        }
        onDispose { }
    }

    val formattedUrl = remember(haIpAddress, schedulePath) {
        val cleanIp = haIpAddress.trim()
            .removePrefix("http://")
            .removePrefix("https://")
            .trimEnd('/')
        val cleanPath = schedulePath.trim().removePrefix("/")
        "http://$cleanIp/$cleanPath"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "HEATING SCHEDULE",
                        color = currentTextColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                },
                navigationIcon = {
                    TextButton(onClick = onBackClick) {
                        Text(
                            text = "< BACK",
                            color = neonCyan,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = currentBgColor
                )
            )
        },
        containerColor = currentBgColor
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(currentBgColor)
        ) {
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )

                        // Set layer type to enable WebGL and Shadow DOM overlays
                        setLayerType(View.LAYER_TYPE_HARDWARE, null)

                        isClickable = true
                        isFocusable = true
                        isFocusableInTouchMode = true
                        requestFocusFromTouch()

                        // Configure Cookie Manager to preserve Home Assistant sessions
                        val cookieManager = CookieManager.getInstance()
                        cookieManager.setAcceptCookie(true)
                        cookieManager.setAcceptThirdPartyCookies(this, true)

                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                super.onPageStarted(view, url, favicon)
                                isLoading = true
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                isLoading = false
                            }

                            override fun onRenderProcessGone(
                                view: WebView?,
                                detail: RenderProcessGoneDetail?
                            ): Boolean {
                                // Prevents the app from crashing if WebKit crashes in background
                                return true
                            }
                        }

                        webChromeClient = object : WebChromeClient() {
                            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                                return super.onConsoleMessage(consoleMessage)
                            }
                        }

                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            databaseEnabled = true
                            allowFileAccess = true
                            allowContentAccess = true

                            // Required to allow LitElement/ShadowDOM custom dialog popups:
                            javaScriptCanOpenWindowsAutomatically = true
                            setSupportMultipleWindows(false)

                            useWideViewPort = true
                            loadWithOverviewMode = false
                            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

                            // Standard Chrome user agent to prevent HA frontend breaking dialog event listeners
                            userAgentString = "Mozilla/5.0 (Linux; Android 13; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
                        }

                        loadUrl(formattedUrl)
                    }
                },
                update = { webView ->
                    if (webView.url != formattedUrl) {
                        webView.loadUrl(formattedUrl)
                    }
                },
                onRelease = { webView ->
                    // Clean up Webview lifecycle when navigating away to prevent background crashes
                    webView.stopLoading()
                    webView.destroy()
                },
                modifier = Modifier.fillMaxSize()
            )

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = neonCyan
                )
            }
        }
    }
}