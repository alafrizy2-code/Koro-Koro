package com.example.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.local.PlaylistEntity
import com.example.model.KaraokeSong
import com.example.model.UsbMicStatus
import com.example.model.VocalPreset
import com.example.model.VocalSettings
import com.example.ui.components.TvFocusableSurface
import com.example.ui.theme.KaraokeCrimson
import com.example.ui.theme.KaraokeCyan
import com.example.ui.theme.KaraokeEmerald
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TvBackground
import com.example.ui.theme.TvSurface
import com.example.ui.theme.TvSurfaceElevated
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun KaraokePlayerScreen(
    song: KaraokeSong,
    relatedQueue: List<KaraokeSong>,
    isFavorite: Boolean,
    playlists: List<PlaylistEntity>,
    vocalSettings: VocalSettings,
    usbMicStatus: UsbMicStatus,
    presetHudMessage: String?,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onAddSongToPlaylist: (Long) -> Unit,
    onPlayNextSong: (KaraokeSong) -> Unit,
    onUpdatePosition: (Int) -> Unit,
    onApplyPreset: (VocalPreset) -> Unit,
    onClearPresetHud: () -> Unit,
    onUpdateVocalSettings: ((VocalSettings) -> VocalSettings) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(true) }
    var currentSeconds by remember { mutableIntStateOf(song.lastPositionSeconds) }
    var totalSeconds by remember { mutableIntStateOf(song.durationSeconds.coerceAtLeast(1)) }
    var showControls by remember { mutableStateOf(true) }
    var showMicDrawer by remember { mutableStateOf(false) }
    var showPlaylistDialog by remember { mutableStateOf(false) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    val playPauseFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        try {
            playPauseFocusRequester.requestFocus()
        } catch (_: Exception) {
        }
    }

    // Auto-dismiss 0-9 preset HUD toast
    LaunchedEffect(presetHudMessage) {
        if (presetHudMessage != null) {
            delay(2400)
            onClearPresetHud()
        }
    }

    // Periodically save playback position
    LaunchedEffect(song.videoId, currentSeconds) {
        if (currentSeconds > 0 && currentSeconds % 5 == 0) {
            onUpdatePosition(currentSeconds)
        }
    }

    BackHandler {
        when {
            showMicDrawer -> showMicDrawer = false
            showPlaylistDialog -> showPlaylistDialog = false
            else -> onBack()
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
                    val digit = mapKeyToDigit(event.key)
                    if (digit != null) {
                        VocalPreset.fromKeyNumber(digit)?.let { preset ->
                            onApplyPreset(preset)
                        }
                        return@onPreviewKeyEvent true
                    }
                    if (event.key == Key.MediaPlayPause) {
                        togglePlayPause()
                        return@onPreviewKeyEvent true
                    }
                }
                false
            }
    ) {
        // Official YouTube IFrame Player WebView
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setBackgroundColor(android.graphics.Color.BLACK)
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.mediaPlaybackRequiresUserGesture = false
                    settings.cacheMode = WebSettings.LOAD_DEFAULT
                    webChromeClient = WebChromeClient()
                    webViewClient = WebViewClient()

                    addJavascriptInterface(
                        object {
                            @JavascriptInterface
                            fun onStateChange(state: Int) {
                                isPlaying = (state == 1 || state == 3)
                            }

                            @JavascriptInterface
                            fun onTimeUpdate(cur: Int, dur: Int) {
                                currentSeconds = cur
                                if (dur > 0) totalSeconds = dur
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
                    wv.loadDataWithBaseURL("https://www.youtube.com", html, "text/html", "UTF-8", null)
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

        // Top & Bottom Gradient Scrim when controls are visible
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

        // Top Bar: Back to Home, Song Info, Active Vocal Preset & Mic Drawer Trigger
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
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
                            text = "${song.singer} • Tekan tombol angka 0–9 di remote untuk ganti efek vokal",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Active Vocal Preset & Mic Settings Button
                TvFocusableSurface(
                    onClick = { showMicDrawer = !showMicDrawer },
                    testTag = "player_mic_settings_button",
                    shape = RoundedCornerShape(24.dp),
                    containerColor = TvSurfaceElevated.copy(alpha = 0.9f),
                    focusedContainerColor = KaraokeCyan
                ) { isFocused ->
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(
                                    if (vocalSettings.micMonitorEnabled) KaraokeEmerald else TextSecondary
                                )
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Icon(
                            imageVector = Icons.Filled.Mic,
                            contentDescription = "Pengaturan Mikrofon",
                            tint = if (isFocused) TvBackground else KaraokeCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "[${vocalSettings.preset.keyNumber}] ${vocalSettings.preset.displayName}",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isFocused) TvBackground else TextPrimary
                        )
                    }
                }
            }
        }

        // Numeric 0-9 Remote Preset HUD Notification
        AnimatedVisibility(
            visible = presetHudMessage != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 84.dp, end = 28.dp)
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(TvSurface.copy(alpha = 0.94f))
                    .border(1.5.dp, KaraokeCyan, RoundedCornerShape(16.dp))
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.GraphicEq,
                    contentDescription = null,
                    tint = KaraokeCyan,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = presetHudMessage.orEmpty(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
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
                // Progress Bar
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

                // Action Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Play / Pause
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

                        // Replay 10s
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

                        // Forward 10s
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

                        // Next Song in Queue
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
                        // Favorite Toggle
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

                        // Add to Playlist
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

                        // Open in Official YouTube App (Fallback)
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

        // Slide-over Microphone & Vocal Effects Drawer
        AnimatedVisibility(
            visible = showMicDrawer,
            enter = slideInHorizontally { it } + fadeIn(),
            exit = slideOutHorizontally { it } + fadeOut(),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Column(
                modifier = Modifier
                    .width(420.dp)
                    .fillMaxHeight()
                    .background(TvSurface.copy(alpha = 0.96f))
                    .border(1.dp, KaraokeCyan.copy(alpha = 0.4f))
                    .padding(22.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Mikrofon & Efek Vokal",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary
                        )
                        Text(
                            text = usbMicStatus.deviceName,
                            style = MaterialTheme.typography.labelSmall,
                            color = KaraokeCyan
                        )
                    }
                    TvFocusableSurface(
                        onClick = { showMicDrawer = false },
                        shape = CircleShape,
                        containerColor = TvSurfaceElevated,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Tutup Panel Mic",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    // Presets 0-9
                    item {
                        Text(
                            text = "Preset Cepat (Tombol Remote 0–9)",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(VocalPreset.entries, key = { it.keyNumber }) { preset ->
                                val selected = vocalSettings.preset == preset
                                TvFocusableSurface(
                                    onClick = { onApplyPreset(preset) },
                                    shape = RoundedCornerShape(12.dp),
                                    containerColor = if (selected) KaraokeCrimson else TvSurfaceElevated,
                                    focusedContainerColor = KaraokeCrimson
                                ) {
                                    Text(
                                        text = "[${preset.keyNumber}] ${preset.displayName}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Mic Monitor ON/OFF
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(TvSurfaceElevated)
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Mic Monitor (Output TV)",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Switch(
                                checked = vocalSettings.micMonitorEnabled,
                                onCheckedChange = { enabled ->
                                    onUpdateVocalSettings { it.copy(micMonitorEnabled = enabled) }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = KaraokeCrimson
                                )
                            )
                        }
                    }

                    item {
                        AudioStepperRow(
                            label = "Monitor Vol",
                            valueText = "${(vocalSettings.micMonitorVolume * 100).roundToInt()}%",
                            onDecrease = {
                                onUpdateVocalSettings {
                                    it.copy(micMonitorVolume = (it.micMonitorVolume - 0.1f).coerceIn(0f, 1f))
                                }
                            },
                            onIncrease = {
                                onUpdateVocalSettings {
                                    it.copy(micMonitorVolume = (it.micMonitorVolume + 0.1f).coerceIn(0f, 1f))
                                }
                            }
                        )
                    }

                    item {
                        AudioStepperRow(
                            label = "Mic Volume",
                            valueText = "${(vocalSettings.micVolume * 100).roundToInt()}%",
                            onDecrease = {
                                onUpdateVocalSettings {
                                    it.copy(micVolume = (it.micVolume - 0.1f).coerceIn(0f, 2f))
                                }
                            },
                            onIncrease = {
                                onUpdateVocalSettings {
                                    it.copy(micVolume = (it.micVolume + 0.1f).coerceIn(0f, 2f))
                                }
                            }
                        )
                    }

                    item {
                        AudioStepperRow(
                            label = "Echo",
                            valueText = "${(vocalSettings.echoLevel * 100).roundToInt()}%",
                            onDecrease = {
                                onUpdateVocalSettings {
                                    it.copy(
                                        preset = VocalPreset.CUSTOM_EFFECT,
                                        echoLevel = (it.echoLevel - 0.1f).coerceIn(0f, 1f)
                                    )
                                }
                            },
                            onIncrease = {
                                onUpdateVocalSettings {
                                    it.copy(
                                        preset = VocalPreset.CUSTOM_EFFECT,
                                        echoLevel = (it.echoLevel + 0.1f).coerceIn(0f, 1f)
                                    )
                                }
                            }
                        )
                    }

                    item {
                        AudioStepperRow(
                            label = "Reverb",
                            valueText = "${(vocalSettings.reverbLevel * 100).roundToInt()}%",
                            onDecrease = {
                                onUpdateVocalSettings {
                                    it.copy(
                                        preset = VocalPreset.CUSTOM_EFFECT,
                                        reverbLevel = (it.reverbLevel - 0.1f).coerceIn(0f, 1f)
                                    )
                                }
                            },
                            onIncrease = {
                                onUpdateVocalSettings {
                                    it.copy(
                                        preset = VocalPreset.CUSTOM_EFFECT,
                                        reverbLevel = (it.reverbLevel + 0.1f).coerceIn(0f, 1f)
                                    )
                                }
                            }
                        )
                    }

                    item {
                        AudioStepperRow(
                            label = "Bass",
                            valueText = "${vocalSettings.bassDb.roundToInt()} dB",
                            onDecrease = {
                                onUpdateVocalSettings {
                                    it.copy(
                                        preset = VocalPreset.CUSTOM_EFFECT,
                                        bassDb = (it.bassDb - 2f).coerceIn(-12f, 12f)
                                    )
                                }
                            },
                            onIncrease = {
                                onUpdateVocalSettings {
                                    it.copy(
                                        preset = VocalPreset.CUSTOM_EFFECT,
                                        bassDb = (it.bassDb + 2f).coerceIn(-12f, 12f)
                                    )
                                }
                            }
                        )
                    }

                    item {
                        AudioStepperRow(
                            label = "Treble",
                            valueText = "${vocalSettings.trebleDb.roundToInt()} dB",
                            onDecrease = {
                                onUpdateVocalSettings {
                                    it.copy(
                                        preset = VocalPreset.CUSTOM_EFFECT,
                                        trebleDb = (it.trebleDb - 2f).coerceIn(-12f, 12f)
                                    )
                                }
                            },
                            onIncrease = {
                                onUpdateVocalSettings {
                                    it.copy(
                                        preset = VocalPreset.CUSTOM_EFFECT,
                                        trebleDb = (it.trebleDb + 2f).coerceIn(-12f, 12f)
                                    )
                                }
                            }
                        )
                    }

                    item {
                        AudioStepperRow(
                            label = "Pitch",
                            valueText = "${if (vocalSettings.pitchSemitones > 0) "+" else ""}${vocalSettings.pitchSemitones}",
                            onDecrease = {
                                onUpdateVocalSettings {
                                    it.copy(
                                        preset = VocalPreset.CUSTOM_EFFECT,
                                        pitchSemitones = (it.pitchSemitones - 1).coerceIn(-6, 6)
                                    )
                                }
                            },
                            onIncrease = {
                                onUpdateVocalSettings {
                                    it.copy(
                                        preset = VocalPreset.CUSTOM_EFFECT,
                                        pitchSemitones = (it.pitchSemitones + 1).coerceIn(-6, 6)
                                    )
                                }
                            }
                        )
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

private fun mapKeyToDigit(key: Key): Int? {
    return when (key) {
        Key.Zero, Key.NumPad0 -> 0
        Key.One, Key.NumPad1 -> 1
        Key.Two, Key.NumPad2 -> 2
        Key.Three, Key.NumPad3 -> 3
        Key.Four, Key.NumPad4 -> 4
        Key.Five, Key.NumPad5 -> 5
        Key.Six, Key.NumPad6 -> 6
        Key.Seven, Key.NumPad7 -> 7
        Key.Eight, Key.NumPad8 -> 8
        Key.Nine, Key.NumPad9 -> 9
        else -> null
    }
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
            </style>
        </head>
        <body>
            <div id="player"></div>
            <script src="https://www.youtube.com/iframe_api"></script>
            <script>
                var ytPlayer;
                function onYouTubeIframeAPIReady() {
                    ytPlayer = new YT.Player('player', {
                        videoId: '$videoId',
                        playerVars: {
                            'autoplay': 1,
                            'controls': 0,
                            'rel': 0,
                            'modestbranding': 1,
                            'iv_load_policy': 3,
                            'playsinline': 1,
                            'start': $startSeconds
                        },
                        events: {
                            'onStateChange': function(e) {
                                if (window.KoroBridge) KoroBridge.onStateChange(e.data);
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
