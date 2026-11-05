package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.SidebarDestination
import com.example.model.VocalPreset
import com.example.ui.MainViewModel
import com.example.ui.components.ExternalPlayerDialog
import com.example.ui.components.SidebarNav
import com.example.ui.components.TopSearchBar
import com.example.ui.screens.CategoriesScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.KaraokePlayerScreen
import com.example.ui.screens.PlaylistsScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.KaraokeCyan
import com.example.ui.theme.KoroKoroTvTheme
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TvBackground
import com.example.ui.theme.TvSurface
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val startupMicPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { _ -> }

            LaunchedEffect(Unit) {
                if (ContextCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.RECORD_AUDIO
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    startupMicPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            }

            KoroKoroTvTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val vocalSettings by viewModel.vocalSettings.collectAsStateWithLifecycle()
                val voiceLanguage by viewModel.voiceLanguage.collectAsStateWithLifecycle()
                val usbMicStatus by viewModel.usbMicStatus.collectAsStateWithLifecycle()
                val voiceSearchState by viewModel.voiceSearchState.collectAsStateWithLifecycle()
                val favorites by viewModel.favorites.collectAsStateWithLifecycle()
                val history by viewModel.history.collectAsStateWithLifecycle()
                val playlists by viewModel.playlists.collectAsStateWithLifecycle()

                val favoriteIds = remember(favorites) {
                    favorites.map { it.videoId }.toSet()
                }

                val tizenTubeApp = remember(uiState.currentDestination) {
                    viewModel.externalPlayerManager.findTizenTubeCobalt()
                }

                LaunchedEffect(uiState.presetHudMessage) {
                    if (uiState.presetHudMessage != null) {
                        delay(2400)
                        viewModel.clearPresetHud()
                    }
                }

                BackHandler(
                    enabled = uiState.activeInternalPlayerSong == null &&
                        uiState.externalLaunchResult == null &&
                        uiState.currentDestination != SidebarDestination.HOME
                ) {
                    viewModel.navigateTo(SidebarDestination.HOME)
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(TvBackground)
                        .windowInsetsPadding(WindowInsets.safeDrawing)
                ) {
                    val internalSong = uiState.activeInternalPlayerSong
                    if (internalSong != null) {
                        KaraokePlayerScreen(
                            song = internalSong,
                            relatedQueue = uiState.relatedQueue,
                            isFavorite = favoriteIds.contains(internalSong.videoId),
                            playlists = playlists,
                            onBack = { viewModel.closeInternalPlayer() },
                            onToggleFavorite = {
                                viewModel.toggleFavorite(
                                    internalSong,
                                    favoriteIds.contains(internalSong.videoId)
                                )
                            },
                            onAddSongToPlaylist = { playlistId ->
                                viewModel.addSongToPlaylist(playlistId, internalSong)
                            },
                            onPlayNextSong = { next ->
                                viewModel.playSong(next, uiState.relatedQueue)
                            },
                            onUpdatePosition = { pos ->
                                viewModel.updatePlaybackPosition(internalSong.videoId, pos)
                            }
                        )
                    } else {
                        Row(modifier = Modifier.fillMaxSize()) {
                            SidebarNav(
                                currentDestination = uiState.currentDestination,
                                isExpanded = uiState.isSidebarFocused,
                                isUsbMicConnected = usbMicStatus.isUsbConnected,
                                onSidebarFocusChanged = { focused ->
                                    viewModel.setSidebarFocused(focused)
                                },
                                onSelectDestination = { dest ->
                                    viewModel.navigateTo(dest)
                                }
                            )

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            ) {
                                TopSearchBar(
                                    currentQuery = uiState.searchQuery,
                                    isLiveYouTubeApi = uiState.isLiveYouTubeApi,
                                    onOpenTextSearch = {
                                        viewModel.navigateTo(SidebarDestination.SEARCH)
                                    },
                                    onOpenVoiceSearch = {
                                        viewModel.openVoiceSearchModal()
                                    }
                                )

                                Box(modifier = Modifier.weight(1f)) {
                                    when (uiState.currentDestination) {
                                        SidebarDestination.HOME -> {
                                            HomeScreen(
                                                sections = uiState.homeSections,
                                                history = history,
                                                favoriteIds = favoriteIds,
                                                singers = viewModel.singers,
                                                isLoading = uiState.isLoadingHome,
                                                isLiveYouTubeApi = uiState.isLiveYouTubeApi,
                                                onPlaySong = { song, queue ->
                                                    viewModel.playSong(song, queue)
                                                },
                                                onSelectSinger = { singer ->
                                                    viewModel.searchBySinger(singer)
                                                },
                                                onOpenVoiceSearch = {
                                                    viewModel.openVoiceSearchModal()
                                                }
                                            )
                                        }

                                        SidebarDestination.SEARCH,
                                        SidebarDestination.VOICE_SEARCH -> {
                                            SearchScreen(
                                                query = uiState.searchQuery,
                                                karaokeFilterEnabled = uiState.karaokeFilterEnabled,
                                                results = uiState.searchResults,
                                                isSearching = uiState.isSearching,
                                                showVoiceOverlay = uiState.showVoiceOverlay,
                                                voiceSearchState = voiceSearchState,
                                                voiceLanguage = voiceLanguage,
                                                favoriteIds = favoriteIds,
                                                voiceSearchManager = viewModel.voiceSearchManager,
                                                onQueryChange = { viewModel.updateSearchQuery(it) },
                                                onSearch = { viewModel.performSearch(it) },
                                                onToggleKaraokeFilter = { viewModel.toggleKaraokeFilter() },
                                                onOpenVoiceOverlay = { viewModel.openVoiceSearchModal() },
                                                onCloseVoiceOverlay = { viewModel.closeVoiceSearchModal() },
                                                onSetVoiceLanguage = { viewModel.setVoiceLanguage(it) },
                                                onPlaySong = { song, queue ->
                                                    viewModel.playSong(song, queue)
                                                }
                                            )
                                        }

                                        SidebarDestination.CATEGORIES -> {
                                            CategoriesScreen(
                                                categories = viewModel.categories,
                                                selectedCategory = uiState.selectedCategory,
                                                categorySongs = uiState.categorySongs,
                                                isLoading = uiState.isLoadingCategory,
                                                favoriteIds = favoriteIds,
                                                onSelectCategory = { viewModel.selectCategory(it) },
                                                onPlaySong = { song, queue ->
                                                    viewModel.playSong(song, queue)
                                                }
                                            )
                                        }

                                        SidebarDestination.FAVORITES -> {
                                            FavoritesScreen(
                                                favorites = favorites,
                                                onPlaySong = { song, queue ->
                                                    viewModel.playSong(song, queue)
                                                }
                                            )
                                        }

                                        SidebarDestination.HISTORY -> {
                                            HistoryScreen(
                                                history = history,
                                                favoriteIds = favoriteIds,
                                                onPlaySong = { song, queue ->
                                                    viewModel.playSong(song, queue)
                                                },
                                                onClearHistory = { viewModel.clearHistory() }
                                            )
                                        }

                                        SidebarDestination.PLAYLISTS -> {
                                            PlaylistsScreen(
                                                playlists = playlists,
                                                favoriteIds = favoriteIds,
                                                getSongsInPlaylist = { id ->
                                                    viewModel.getSongsInPlaylist(id)
                                                },
                                                onCreatePlaylist = { name ->
                                                    viewModel.createPlaylist(name)
                                                },
                                                onDeletePlaylist = { id ->
                                                    viewModel.deletePlaylist(id)
                                                },
                                                onPlaySong = { song, queue ->
                                                    viewModel.playSong(song, queue)
                                                }
                                            )
                                        }

                                        SidebarDestination.SETTINGS -> {
                                            SettingsScreen(
                                                vocalSettings = vocalSettings,
                                                usbMicStatus = usbMicStatus,
                                                tizenTubeApp = tizenTubeApp,
                                                voiceLanguage = voiceLanguage,
                                                isLiveYouTubeApi = uiState.isLiveYouTubeApi,
                                                onApplyPreset = { viewModel.applyVocalPreset(it) },
                                                onUpdateVocalSettings = { viewModel.updateVocalSettings(it) },
                                                onToggleEngine = { viewModel.toggleAudioEngine() },
                                                onRefreshUsbMic = { viewModel.usbAudioEngine.refreshUsbMicDetection() },
                                                onSetVoiceLanguage = { viewModel.setVoiceLanguage(it) }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // TizenTube Cobalt External Player Dialog
                        val selectedSong = uiState.selectedSongForExternal
                        val launchRes = uiState.externalLaunchResult
                        if (selectedSong != null && launchRes != null) {
                            ExternalPlayerDialog(
                                song = selectedSong,
                                launchResult = launchRes,
                                isFavorite = favoriteIds.contains(selectedSong.videoId),
                                activePreset = vocalSettings.preset,
                                onRetryTizenTube = { viewModel.retryTizenTubeForSelected() },
                                onSelectAlternativePlayer = { app ->
                                    viewModel.launchAlternativePlayer(app)
                                },
                                onOpenSystemChooser = { viewModel.launchSystemChooser() },
                                onOpenInternalPlayer = { viewModel.openInternalPlayerFallback() },
                                onToggleFavorite = {
                                    viewModel.toggleFavorite(
                                        selectedSong,
                                        favoriteIds.contains(selectedSong.videoId)
                                    )
                                },
                                onDismiss = { viewModel.dismissExternalDialog() }
                            )
                        }
                    }

                    // 0-9 Numeric Remote Vocal Preset HUD
                    AnimatedVisibility(
                        visible = uiState.presetHudMessage != null,
                        enter = fadeIn(),
                        exit = fadeOut(),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(24.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(TvSurface.copy(alpha = 0.95f))
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
                                text = uiState.presetHudMessage.orEmpty(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOICE_ASSIST ||
            keyCode == KeyEvent.KEYCODE_SEARCH ||
            keyCode == KeyEvent.KEYCODE_MEDIA_RECORD
        ) {
            if (viewModel.uiState.value.activeInternalPlayerSong != null) {
                viewModel.closeInternalPlayer()
            }
            viewModel.openVoiceSearchModal()
            return true
        }

        // Numeric 0-9 remote keys for instant Vocal Preset switching
        val digit = when (keyCode) {
            KeyEvent.KEYCODE_0, KeyEvent.KEYCODE_NUMPAD_0 -> 0
            KeyEvent.KEYCODE_1, KeyEvent.KEYCODE_NUMPAD_1 -> 1
            KeyEvent.KEYCODE_2, KeyEvent.KEYCODE_NUMPAD_2 -> 2
            KeyEvent.KEYCODE_3, KeyEvent.KEYCODE_NUMPAD_3 -> 3
            KeyEvent.KEYCODE_4, KeyEvent.KEYCODE_NUMPAD_4 -> 4
            KeyEvent.KEYCODE_5, KeyEvent.KEYCODE_NUMPAD_5 -> 5
            KeyEvent.KEYCODE_6, KeyEvent.KEYCODE_NUMPAD_6 -> 6
            KeyEvent.KEYCODE_7, KeyEvent.KEYCODE_NUMPAD_7 -> 7
            KeyEvent.KEYCODE_8, KeyEvent.KEYCODE_NUMPAD_8 -> 8
            KeyEvent.KEYCODE_9, KeyEvent.KEYCODE_NUMPAD_9 -> 9
            else -> null
        }
        if (digit != null) {
            VocalPreset.fromKeyNumber(digit)?.let { preset ->
                viewModel.applyVocalPreset(preset)
                return true
            }
        }

        return super.onKeyDown(keyCode, event)
    }
}
