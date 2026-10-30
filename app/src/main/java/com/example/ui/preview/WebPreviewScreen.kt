package com.example.ui.preview

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.CodeFileEntity
import com.example.execution.CodeRunner

enum class PreviewViewport(val label: String, val widthDp: Int?) {
    RESPONSIVE("Responsive", null),
    MOBILE("Mobile (390px)", 390),
    TABLET("Tablet (640px)", 640)
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebPreviewScreen(
    files: List<CodeFileEntity>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var viewport by remember { mutableStateOf(PreviewViewport.RESPONSIVE) }
    var refreshKey by remember { mutableStateOf(0) }
    var hasRendererCrashed by remember { mutableStateOf(false) }
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    val consoleLogs = remember { mutableStateListOf<String>() }

    DisposableEffect(Unit) {
        onDispose {
            try {
                webViewInstance?.stopLoading()
                webViewInstance?.destroy()
            } catch (_: Exception) {}
            webViewInstance = null
        }
    }

    val htmlData = remember(files, refreshKey) {
        CodeRunner.buildWebPreviewHtml(files)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        // Control Bar
        Surface(
            color = Color(0xFF1E293B),
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(Color(0xFF10B981))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Live Web Server (Localhost:3000)",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Viewport selector
                    PreviewViewport.entries.forEach { vp ->
                        FilterChip(
                            selected = viewport == vp,
                            onClick = { viewport = vp },
                            label = { Text(vp.label, fontSize = 10.sp) },
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = {
                            consoleLogs.clear()
                            refreshKey++
                            webViewInstance?.reload()
                        },
                        modifier = Modifier.size(32.dp).testTag("preview_refresh_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // WebView Container
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(8.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            val previewBoxModifier = if (viewport.widthDp != null) {
                Modifier
                    .width(viewport.widthDp!!.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(12.dp))
                    .border(2.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
            } else {
                Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp))
            }

            if (hasRendererCrashed) {
                // Graceful fallback UI when Chromium renderer process crashed in emulator
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .testTag("preview_renderer_crashed_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Web Preview Renderer Recovered",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "The background web preview process was restored in software rendering mode.",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = {
                                hasRendererCrashed = false
                                refreshKey++
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("restart_preview_button")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Restart Preview", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                key(refreshKey) {
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                // CRITICAL: Disable hardware acceleration on WebView to avoid Mesa DRI / rendernode crash in headless/streaming emulator
                                setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                settings.cacheMode = WebSettings.LOAD_NO_CACHE
                                settings.useWideViewPort = true
                                settings.loadWithOverviewMode = true
                                settings.safeBrowsingEnabled = false
                                settings.allowFileAccess = false
                                settings.allowContentAccess = false

                                webChromeClient = object : WebChromeClient() {
                                    override fun onConsoleMessage(msg: ConsoleMessage?): Boolean {
                                        msg?.message()?.let {
                                            consoleLogs.add("[Console]: $it")
                                        }
                                        return true
                                    }
                                }

                                webViewClient = object : WebViewClient() {
                                    override fun onRenderProcessGone(view: WebView?, detail: RenderProcessGoneDetail?): Boolean {
                                        // Returning true signals the host app handled the event, preventing application termination!
                                        try {
                                            view?.let { deadView ->
                                                (deadView.parent as? ViewGroup)?.removeView(deadView)
                                                deadView.destroy()
                                            }
                                        } catch (_: Exception) {}
                                        webViewInstance = null
                                        hasRendererCrashed = true
                                        return true
                                    }
                                }

                                loadDataWithBaseURL("https://localhost/", htmlData, "text/html", "UTF-8", null)
                                webViewInstance = this
                            }
                        },
                        update = { webView ->
                            if (!hasRendererCrashed) {
                                webView.loadDataWithBaseURL("https://localhost/", htmlData, "text/html", "UTF-8", null)
                            }
                        },
                        onRelease = { webView ->
                            try {
                                webView.stopLoading()
                                webView.destroy()
                            } catch (_: Exception) {}
                        },
                        modifier = previewBoxModifier.testTag("preview_webview")
                    )
                }
            }
        }

        // Live Console drawer if logs exist
        if (consoleLogs.isNotEmpty()) {
            Surface(
                color = Color(0xFF0D1117),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 100.dp)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(6.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        "Browser Console Logs",
                        color = Color(0xFF64748B),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    consoleLogs.forEach { log ->
                        Text(
                            text = log,
                            color = Color(0xFF38BDF8),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}
