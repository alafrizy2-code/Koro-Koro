package com.example.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.local.PlaylistEntity
import com.example.model.KaraokeSong
import com.example.ui.components.TvFocusableSurface
import com.example.ui.theme.KaraokeCrimson
import com.example.ui.theme.KaraokeCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TvBackground
import com.example.ui.theme.TvSurface
import com.example.ui.theme.TvSurfaceElevated
import java.io.ByteArrayInputStream

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun KaraokePlayerScreen(
    song: KaraokeSong,
    relatedQueue: List<KaraokeSong>,
    isFavorite: Boolean,
    playlists: List<PlaylistEntity>,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onAddSongToPlaylist: (Long) -> Unit,
    onPlayNextSong: (KaraokeSong) -> Unit,
    onUpdatePosition: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(true) }
    var currentSeconds by remember { mutableIntStateOf(song.lastPositionSeconds) }
    var totalSeconds by remember { mutableIntStateOf(song.durationSeconds.coerceAtLeast(1)) }
    var showControls by remember { mutableStateOf(true) }
    var showPlaylistDialog by remember { mutableStateOf(false) }
    var embedErrorCode by remember { mutableStateOf<Int?>(null) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    val playPauseFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        try {
            playPauseFocusRequester.requestFocus()
        } catch (_: Exception) {
        }
    }

    LaunchedEffect(song.videoId, currentSeconds) {
        if (currentSeconds > 0 && currentSeconds % 5 == 0) {
            onUpdatePosition(currentSeconds)
        }
    }

    BackHandler {
        if (showPlaylistDialog) {
            showPlaylistDialog = false
        } else {
            onBack()
        }
    }

    fun togglePlayPause() {
        val wv = webViewRef ?: return
        if (isPlaying) {
            wv.evaluateJavascript("if(window.ytPlayer && ytPlayer.pauseVideo) ytPlayer.pauseVideo();", null)
            isPlaying = false
        } else {
            wv.evaluateJavascript("if(window.ytPlayer && ytPlayer.playVideo) ytPlayer.playVideo();", null)
            isPlaying = true
        }
    }

    fun seekBy(deltaSeconds: Int) {
        val wv = webViewRef ?: return
        val target = (currentSeconds + deltaSeconds).coerceIn(0, totalSeconds)
        currentSeconds = target
        wv.evaluateJavascript("if(window.ytPlayer && ytPlayer.seekTo) ytPlayer.seekTo($target, true);", null)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .onPreviewKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown) {
                    showControls = true
                    if (event.key == Key.MediaPlayPause) {
                        togglePlayPause()
                        return@onPreviewKeyEvent true
                    }
                }
                false
            }
    ) {
        // Ad-free YouTube IFrame Player WebView
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setBackgroundColor(android.graphics.Color.BLACK)
                    setLayerType(View.LAYER_TYPE_HARDWARE, null)
                    CookieManager.getInstance().setAcceptCookie(true)
                    CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.mediaPlaybackRequiresUserGesture = false
                    settings.javaScriptCanOpenWindowsAutomatically = true
                    settings.cacheMode = WebSettings.LOAD_DEFAULT
                    webChromeClient = WebChromeClient()
                    webViewClient = object : WebViewClient() {
                        override fun shouldInterceptRequest(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): WebResourceResponse? {
                            val url = request?.url?.toString().orEmpty()
                            if (isAdUrl(url)) {
                                return WebResourceResponse(
                                    "text/plain",
                                    "utf-8",
                                    ByteArrayInputStream(ByteArray(0))
                                )
                            }
                            return super.shouldInterceptRequest(view, request)
                        }
                    }

                    addJavascriptInterface(
                        object {
                            @JavascriptInterface
                            fun onStateChange(state: Int) {
                                isPlaying = (state == 1 || state == 3)
                                if (state == 1) {
                                    embedErrorCode = null
                                }
                            }

                            @JavascriptInterface
                            fun onTimeUpdate(cur: Int, dur: Int) {
                                currentSeconds = cur
                                if (dur > 0) totalSeconds = dur
                            }

                            @JavascriptInterface
                            fun onPlayerError(code: Int) {
                                embedErrorCode = code
                            }
                        },
                        "KoroBridge"
                    )
                    webViewRef = this
                }
            },
            update = { wv ->
                val html = buildYouTubeIFrameHtml(song.videoId, song.lastPositionSeconds)
                if (wv.tag != song.videoId) {
                    wv.tag = song.videoId
                    embedErrorCode = null
                    wv.loadDataWithBaseURL("https://korokoro.tv", html, "text/html", "UTF-8", null)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        DisposableEffect(Unit) {
            onDispose {
                webViewRef?.destroy()
                webViewRef = null
            }
        }

        // Graceful Embed Error (150 / 152 / 101) Recovery Overlay
        val errCode = embedErrorCode
        if (errCode != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.88f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .width(520.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(TvSurface)
                        .border(1.5.dp, KaraokeCrimson, RoundedCornerShape(22.dp))
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Video Tidak Dapat Diputar Langsung (Kode $errCode)",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Pemilik video membatasi pemutaran embed di luar aplikasi YouTube. Pilih lagu karaoke lain atau tonton langsung di aplikasi YouTube.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val nextSong = relatedQueue.firstOrNull()
                        if (nextSong != null) {
                            TvFocusableSurface(
                                onClick = { onPlayNextSong(nextSong) },
                                shape = RoundedCornerShape(22.dp),
                                containerColor = KaraokeCrimson,
                                focusedContainerColor = KaraokeCrimson
                            ) {
                                Text(
                                    text = "Putar Lagu Berikutnya",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp)
                                )
                            }
                        }
                        TvFocusableSurface(
                            onClick = {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://www.youtube.com/watch?v=${song.videoId}")
                                )
                                context.startActivity(intent)
                            },
                            shape = RoundedCornerShape(22.dp),
                            containerColor = TvSurfaceElevated,
                            focusedContainerColor = KaraokeCyan
                        ) { isFocused ->
                            Text(
                                text = "Tonton di YouTube",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isFocused) TvBackground else TextPrimary,
                                modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp)
                            )
                        }
                    }
                }
            }
        }

        // Top & Bottom Gradient Scrim
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.78f),
                                Color.Transparent,
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.88f)
                            )
                        )
                    )
            )
        }

        // Top Bar
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TvFocusableSurface(
                    onClick = onBack,
                    testTag = "player_back_button",
                    shape = RoundedCornerShape(24.dp),
                    containerColor = TvSurfaceElevated.copy(alpha = 0.85f),
                    focusedContainerColor = KaraokeCrimson
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali ke Beranda",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Beranda",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.width(18.dp))

                Column {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = song.singer,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Bottom Controls Bar
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp, vertical = 22.dp)
            ) {
                val progress = (currentSeconds.toFloat() / totalSeconds.coerceAtLeast(1).toFloat())
                    .coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = KaraokeCrimson,
                    trackColor = Color.White.copy(alpha = 0.25f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatSeconds(currentSeconds),
                        style = MaterialTheme.typography.labelMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = formatSeconds(totalSeconds),
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TvFocusableSurface(
                            onClick = { togglePlayPause() },
                            testTag = "player_play_pause_button",
                            shape = CircleShape,
                            containerColor = KaraokeCrimson,
                            focusedContainerColor = KaraokeCrimson,
                            modifier = Modifier
                                .size(54.dp)
                                .focusRequester(playPauseFocusRequester)
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                    contentDescription = if (isPlaying) "Jeda" else "Putar",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        TvFocusableSurface(
                            onClick = { seekBy(-10) },
                            shape = CircleShape,
                            containerColor = TvSurfaceElevated.copy(alpha = 0.85f),
                            focusedContainerColor = KaraokeCyan,
                            modifier = Modifier.size(48.dp)
                        ) { isFocused ->
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.Replay10,
                                    contentDescription = "Mundur 10 detik",
                                    tint = if (isFocused) TvBackground else Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        TvFocusableSurface(
                            onClick = { seekBy(10) },
                            shape = CircleShape,
                            containerColor = TvSurfaceElevated.copy(alpha = 0.85f),
                            focusedContainerColor = KaraokeCyan,
                            modifier = Modifier.size(48.dp)
                        ) { isFocused ->
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.Forward10,
                                    contentDescription = "Maju 10 detik",
                                    tint = if (isFocused) TvBackground else Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        val nextSong = relatedQueue.firstOrNull()
                        if (nextSong != null) {
                            TvFocusableSurface(
                                onClick = { onPlayNextSong(nextSong) },
                                shape = RoundedCornerShape(24.dp),
                                containerColor = TvSurfaceElevated.copy(alpha = 0.85f),
                                focusedContainerColor = KaraokeCrimson
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.SkipNext,
                                        contentDescription = "Lagu Berikutnya",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Berikutnya",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TvFocusableSurface(
                            onClick = onToggleFavorite,
                            testTag = "player_favorite_button",
                            shape = RoundedCornerShape(24.dp),
                            containerColor = TvSurfaceElevated.copy(alpha = 0.85f),
                            focusedContainerColor = KaraokeCrimson
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                    contentDescription = "Favorit",
                                    tint = if (isFavorite) KaraokeCrimson else Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isFavorite) "Difavoritkan" else "Favorit",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.White
                                )
                            }
                        }

                        TvFocusableSurface(
                            onClick = { showPlaylistDialog = true },
                            testTag = "player_add_playlist_button",
                            shape = RoundedCornerShape(24.dp),
                            containerColor = TvSurfaceElevated.copy(alpha = 0.85f),
                            focusedContainerColor = KaraokeCrimson
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PlaylistAdd,
                                    contentDescription = "Tambah ke Playlist",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Playlist",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.White
                                )
                            }
                        }

                        TvFocusableSurface(
                            onClick = {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://www.youtube.com/watch?v=${song.videoId}")
                                )
                                context.startActivity(intent)
                            },
                            shape = RoundedCornerShape(24.dp),
                            containerColor = TvSurfaceElevated.copy(alpha = 0.85f),
                            focusedContainerColor = KaraokeCrimson
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.OpenInNew,
                                    contentDescription = "Buka di YouTube",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "YouTube",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        // Add to Playlist Modal
        AnimatedVisibility(
            visible = showPlaylistDialog,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.78f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .width(420.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(TvSurface)
                        .border(1.dp, KaraokeCrimson, RoundedCornerShape(20.dp))
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Simpan ke Playlist",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                    if (playlists.isEmpty()) {
                        Text(
                            text = "Belum ada playlist. Buat playlist terlebih dahulu di menu Playlist.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    } else {
                        playlists.forEach { pl ->
                            TvFocusableSurface(
                                onClick = {
                                    onAddSongToPlaylist(pl.id)
                                    showPlaylistDialog = false
                                },
                                shape = RoundedCornerShape(12.dp),
                                containerColor = TvSurfaceElevated,
                                focusedContainerColor = KaraokeCrimson,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = pl.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    modifier = Modifier.padding(14.dp)
                                )
                            }
                        }
                    }
                    TvFocusableSurface(
                        onClick = { showPlaylistDialog = false },
                        shape = RoundedCornerShape(12.dp),
                        containerColor = TvBackground,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(
                            text = "Tutup",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextSecondary,
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun isAdUrl(url: String): Boolean {
    if (url.isEmpty()) return false
    val lower = url.lowercase()
    return lower.contains("doubleclick.net") ||
        lower.contains("googlesyndication.com") ||
        lower.contains("googleadservices.com") ||
        lower.contains("adservice.google.") ||
        lower.contains("/pagead/") ||
        lower.contains("/api/stats/ads") ||
        lower.contains("/get_midroll_") ||
        lower.contains("/ptracking")
}

private fun formatSeconds(totalSec: Int): String {
    val m = totalSec / 60
    val s = totalSec % 60
    return String.format("%02d:%02d", m, s)
}

private fun buildYouTubeIFrameHtml(videoId: String, startSeconds: Int): String {
    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
            <style>
                html, body { margin: 0; padding: 0; width: 100%; height: 100%; background: #000; overflow: hidden; }
                #player { width: 100%; height: 100%; }
                .ytp-ad-module, .ytp-ad-overlay-container, .ytp-ad-text-overlay { display: none !important; }
            </style>
        </head>
        <body>
            <div id="player"></div>
            <script src="https://www.youtube.com/iframe_api"></script>
            <script>
                var ytPlayer;
                function onYouTubeIframeAPIReady() {
                    ytPlayer = new YT.Player('player', {
                        host: 'https://www.youtube-nocookie.com',
                        videoId: '$videoId',
                        playerVars: {
                            'autoplay': 1,
                            'controls': 0,
                            'rel': 0,
                            'modestbranding': 1,
                            'iv_load_policy': 3,
                            'cc_load_policy': 0,
                            'playsinline': 1,
                            'enablejsapi': 1,
                            'origin': 'https://korokoro.tv',
                            'start': $startSeconds
                        },
                        events: {
                            'onStateChange': function(e) {
                                if (window.KoroBridge) KoroBridge.onStateChange(e.data);
                            },
                            'onError': function(e) {
                                if (window.KoroBridge) KoroBridge.onPlayerError(e.data);
                            }
                        }
                    });
                    setInterval(function() {
                        if (ytPlayer && ytPlayer.getCurrentTime && ytPlayer.getDuration && window.KoroBridge) {
                            var cur = Math.floor(ytPlayer.getCurrentTime() || 0);
                            var dur = Math.floor(ytPlayer.getDuration() || 0);
                            KoroBridge.onTimeUpdate(cur, dur);
                        }
                    }, 1000);
                }
            </script>
        </body>
        </html>
    """.trimIndent()
}
