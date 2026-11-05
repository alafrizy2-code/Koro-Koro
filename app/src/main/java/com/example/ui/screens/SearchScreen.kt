package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.audio.VoiceSearchManager
import com.example.audio.VoiceSearchState
import com.example.model.KaraokeSong
import com.example.ui.components.KaraokeVideoCard
import com.example.ui.components.TvFocusableSurface
import com.example.ui.theme.DividerSubtle
import com.example.ui.theme.KaraokeCrimson
import com.example.ui.theme.KaraokeCyan
import com.example.ui.theme.KaraokeEmerald
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TvBackground
import com.example.ui.theme.TvSurface
import com.example.ui.theme.TvSurfaceElevated
import kotlinx.coroutines.delay

@Composable
fun SearchScreen(
    query: String,
    karaokeFilterEnabled: Boolean,
    results: List<KaraokeSong>,
    isSearching: Boolean,
    showVoiceOverlay: Boolean,
    voiceSearchState: VoiceSearchState,
    voiceLanguage: String,
    favoriteIds: Set<String>,
    voiceSearchManager: VoiceSearchManager,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onToggleKaraokeFilter: () -> Unit,
    onOpenVoiceOverlay: () -> Unit,
    onCloseVoiceOverlay: () -> Unit,
    onSetVoiceLanguage: (String) -> Unit,
    onPlaySong: (KaraokeSong, List<KaraokeSong>) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var permissionDeniedMessage by remember { mutableStateOf<String?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            permissionDeniedMessage = null
            voiceSearchManager.startListening(voiceLanguage)
        } else {
            permissionDeniedMessage = "Izin mikrofon (RECORD_AUDIO) ditolak. Aktifkan izin mikrofon di Pengaturan Android TV atau gunakan pencarian teks."
        }
    }

    fun triggerVoiceRecognition() {
        val hasPerm = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (hasPerm) {
            permissionDeniedMessage = null
            voiceSearchManager.startListening(voiceLanguage)
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // Automatically start listening when Voice Overlay opens
    LaunchedEffect(showVoiceOverlay) {
        if (showVoiceOverlay && voiceSearchState is VoiceSearchState.Idle) {
            triggerVoiceRecognition()
        }
    }

    val quickKeywords = listOf(
        "Pop Indonesia",
        "Dangdut Koplo",
        "Nostalgia 90s",
        "Lagu Barat",
        "Tulus",
        "Dewa 19",
        "Adele",
        "Bruno Mars",
        "Minus One"
    )

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 12.dp)
        ) {
            // Search Input Row + Voice Search Button + Karaoke Filter
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    placeholder = {
                        Text(
                            "Ketik judul lagu, nama penyanyi, atau kata kunci karaoke...",
                            color = TextSecondary
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Cari",
                            tint = KaraokeCrimson
                        )
                    },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            TvFocusableSurface(
                                onClick = { onQueryChange("") },
                                shape = CircleShape,
                                containerColor = Color.Transparent
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Hapus teks",
                                    tint = TextSecondary,
                                    modifier = Modifier.padding(6.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSearch(query) }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = TvSurface,
                        unfocusedContainerColor = TvSurface,
                        focusedBorderColor = KaraokeCrimson,
                        unfocusedBorderColor = DividerSubtle
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("search_text_input")
                )

                TvFocusableSurface(
                    onClick = { onSearch(query) },
                    testTag = "search_submit_button",
                    shape = RoundedCornerShape(16.dp),
                    containerColor = KaraokeCrimson,
                    focusedContainerColor = KaraokeCrimson,
                    focusedBorderColor = Color.White,
                    modifier = Modifier.height(56.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .height(56.dp)
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Cari Sekarang",
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Cari",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White
                        )
                    }
                }

                // Voice Search Button
                TvFocusableSurface(
                    onClick = {
                        onOpenVoiceOverlay()
                        triggerVoiceRecognition()
                    },
                    testTag = "search_voice_button",
                    shape = RoundedCornerShape(16.dp),
                    containerColor = TvSurfaceElevated,
                    focusedContainerColor = KaraokeCyan,
                    focusedBorderColor = Color.White,
                    modifier = Modifier.height(56.dp)
                ) { isFocused ->
                    Row(
                        modifier = Modifier
                            .height(56.dp)
                            .padding(horizontal = 18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Mic,
                            contentDescription = "Pencarian Suara",
                            tint = if (isFocused) TvBackground else KaraokeCyan
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Suara",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isFocused) TvBackground else TextPrimary
                        )
                    }
                }

                // Karaoke Filter Chip
                TvFocusableSurface(
                    onClick = onToggleKaraokeFilter,
                    testTag = "karaoke_filter_toggle",
                    shape = RoundedCornerShape(16.dp),
                    containerColor = if (karaokeFilterEnabled) KaraokeCrimson.copy(alpha = 0.2f) else TvSurface,
                    focusedContainerColor = TvSurfaceElevated,
                    modifier = Modifier.height(56.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .height(56.dp)
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.FilterAlt,
                            contentDescription = "Filter Karaoke",
                            tint = if (karaokeFilterEnabled) KaraokeCrimson else TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (karaokeFilterEnabled) "Filter: Karaoke" else "Semua Video",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (karaokeFilterEnabled) TextPrimary else TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Keyword Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(quickKeywords) { kw ->
                    TvFocusableSurface(
                        onClick = {
                            onQueryChange(kw)
                            onSearch(kw)
                        },
                        shape = RoundedCornerShape(20.dp),
                        containerColor = TvSurfaceElevated,
                        focusedContainerColor = KaraokeCrimson
                    ) { isFocused ->
                        Text(
                            text = kw,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isFocused) Color.White else TextSecondary,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Results Grid
            if (isSearching) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = KaraokeCrimson)
                }
            } else if (results.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Tidak ada video karaoke ditemukan",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary
                        )
                        Text(
                            text = "Coba kata kunci lain atau gunakan Pencarian Suara",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 260.dp),
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    items(results, key = { it.videoId }) { song ->
                        KaraokeVideoCard(
                            song = song,
                            isFavorite = favoriteIds.contains(song.videoId),
                            cardWidth = 280.dp,
                            onClick = { onPlaySong(song, results) }
                        )
                    }
                }
            }
        }

        // Voice Search Modal Overlay
        AnimatedVisibility(visible = showVoiceOverlay) {
            VoiceSearchOverlayPanel(
                state = voiceSearchState,
                voiceLanguage = voiceLanguage,
                permissionDeniedMessage = permissionDeniedMessage,
                onSelectLanguage = { lang ->
                    onSetVoiceLanguage(lang)
                    voiceSearchManager.startListening(lang)
                },
                onRetryListening = { triggerVoiceRecognition() },
                onConfirmSearch = { finalText ->
                    onQueryChange(finalText)
                    onSearch(finalText)
                },
                onClose = onCloseVoiceOverlay
            )
        }
    }
}

@Composable
private fun VoiceSearchOverlayPanel(
    state: VoiceSearchState,
    voiceLanguage: String,
    permissionDeniedMessage: String?,
    onSelectLanguage: (String) -> Unit,
    onRetryListening: () -> Unit,
    onConfirmSearch: (String) -> Unit,
    onClose: () -> Unit
) {
    var editableRecognizedText by remember { mutableStateOf("") }
    var userEditedManually by remember { mutableStateOf(false) }

    LaunchedEffect(state) {
        if (state is VoiceSearchState.ResultReady) {
            editableRecognizedText = state.recognizedText
            userEditedManually = false
            // Auto-execute search if recognition is clear and user hasn't edited
            if (state.isConfident) {
                delay(1200)
                if (!userEditedManually) {
                    onConfirmSearch(state.recognizedText)
                }
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "voice_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(700),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.84f))
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.72f)
                .clip(RoundedCornerShape(24.dp))
                .background(TvSurface)
                .border(1.5.dp, KaraokeCyan.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header + Language Toggle (Indonesia / English)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Pencarian Suara Koro Koro TV",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = "Terisolasi dari Mic Karaoke USB (tidak mengubah efek vokal)",
                        style = MaterialTheme.typography.labelSmall,
                        color = KaraokeEmerald
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TvFocusableSurface(
                        onClick = { onSelectLanguage("id-ID") },
                        testTag = "voice_lang_id",
                        shape = RoundedCornerShape(16.dp),
                        containerColor = if (voiceLanguage == "id-ID") KaraokeCrimson else TvSurfaceElevated
                    ) {
                        Text(
                            text = "Indonesia",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                    TvFocusableSurface(
                        onClick = { onSelectLanguage("en-US") },
                        testTag = "voice_lang_en",
                        shape = RoundedCornerShape(16.dp),
                        containerColor = if (voiceLanguage == "en-US") KaraokeCrimson else TvSurfaceElevated
                    ) {
                        Text(
                            text = "English",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            // Animated Microphone Visual
            val isListening = state is VoiceSearchState.Listening
            Box(contentAlignment = Alignment.Center) {
                if (isListening) {
                    Box(
                        modifier = Modifier
                            .size(104.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(KaraokeCrimson.copy(alpha = 0.25f))
                    )
                }
                TvFocusableSurface(
                    onClick = onRetryListening,
                    testTag = "voice_mic_trigger",
                    shape = CircleShape,
                    containerColor = if (isListening) KaraokeCrimson else TvSurfaceElevated,
                    focusedContainerColor = KaraokeCrimson,
                    modifier = Modifier.size(80.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Filled.GraphicEq else Icons.Filled.Mic,
                            contentDescription = "Mikrofon Pencarian Suara",
                            tint = Color.White,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Status & Recognized Text
            when {
                permissionDeniedMessage != null -> {
                    Text(
                        text = permissionDeniedMessage,
                        style = MaterialTheme.typography.bodyLarge,
                        color = KaraokeCrimson
                    )
                }
                state is VoiceSearchState.Listening -> {
                    Text(
                        text = if (voiceLanguage == "id-ID") "Mendengarkan... Ucapkan judul lagu atau penyanyi"
                        else "Listening... Say a song title or singer name",
                        style = MaterialTheme.typography.titleLarge,
                        color = KaraokeCyan
                    )
                    if (state.partialText.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "\"${state.partialText}\"",
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextPrimary
                        )
                    }
                }
                state is VoiceSearchState.ResultReady -> {
                    Text(
                        text = if (state.isConfident && !userEditedManually) {
                            "Hasil dikenali! Mencari otomatis... (atau koreksi teks di bawah)"
                        } else {
                            "Periksa atau koreksi hasil suara sebelum mencari:"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = KaraokeEmerald
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = editableRecognizedText,
                        onValueChange = {
                            editableRecognizedText = it
                            userEditedManually = true
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = TvBackground,
                            unfocusedContainerColor = TvBackground,
                            focusedBorderColor = KaraokeCyan,
                            unfocusedBorderColor = DividerSubtle
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("voice_result_edit_input")
                    )
                }
                state is VoiceSearchState.Error -> {
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = KaraokeCrimson
                    )
                }
                else -> {
                    Text(
                        text = "Tekan ikon mikrofon untuk mulai berbicara",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (state is VoiceSearchState.ResultReady && editableRecognizedText.isNotBlank()) {
                    TvFocusableSurface(
                        onClick = { onConfirmSearch(editableRecognizedText) },
                        testTag = "voice_confirm_search_button",
                        shape = RoundedCornerShape(22.dp),
                        containerColor = KaraokeCrimson,
                        focusedContainerColor = KaraokeCrimson
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Cari Lagu Ini",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White
                            )
                        }
                    }
                }

                TvFocusableSurface(
                    onClick = onRetryListening,
                    testTag = "voice_retry_button",
                    shape = RoundedCornerShape(22.dp),
                    containerColor = TvSurfaceElevated,
                    focusedContainerColor = KaraokeCyan
                ) { isFocused ->
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = null,
                            tint = if (isFocused) TvBackground else TextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ulangi Suara",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isFocused) TvBackground else TextPrimary
                        )
                    }
                }

                TvFocusableSurface(
                    onClick = onClose,
                    testTag = "voice_close_button",
                    shape = RoundedCornerShape(22.dp),
                    containerColor = TvSurfaceElevated,
                    focusedContainerColor = KaraokeCrimson
                ) {
                    Text(
                        text = "Tutup",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextPrimary,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                    )
                }
            }
        }
    }
}
