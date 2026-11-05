package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.SidebarDestination
import com.example.ui.MainViewModel
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
import com.example.ui.theme.KoroKoroTvTheme
import com.example.ui.theme.TvBackground

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val startupMicPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { _ -> }

            androidx.compose.runtime.LaunchedEffect(Unit) {
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
                val voiceSearchState by viewModel.voiceSearchState.collectAsStateWithLifecycle()
                val favorites by viewModel.favorites.collectAsStateWithLifecycle()
                val history by viewModel.history.collectAsStateWithLifecycle()
                val playlists by viewModel.playlists.collectAsStateWithLifecycle()

                val favoriteIds = remember(favorites) {
                    favorites.map { it.videoId }.toSet()
                }

                BackHandler(
                    enabled = uiState.activePlayerSong == null &&
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
                    val activeSong = uiState.activePlayerSong
                    if (activeSong != null) {
                        KaraokePlayerScreen(
                            song = activeSong,
                            relatedQueue = uiState.relatedQueue,
                            isFavorite = favoriteIds.contains(activeSong.videoId),
                            playlists = playlists,
                            onBack = { viewModel.closePlayer() },
                            onToggleFavorite = {
                                viewModel.toggleFavorite(
                                    activeSong,
                                    favoriteIds.contains(activeSong.videoId)
                                )
                            },
                            onAddSongToPlaylist = { playlistId ->
                                viewModel.addSongToPlaylist(playlistId, activeSong)
                            },
                            onPlayNextSong = { next ->
                                viewModel.playSong(next, uiState.relatedQueue)
                            },
                            onUpdatePosition = { pos ->
                                viewModel.updatePlaybackPosition(activeSong.videoId, pos)
                            }
                        )
                    } else {
                        Row(modifier = Modifier.fillMaxSize()) {
                            SidebarNav(
                                currentDestination = uiState.currentDestination,
                                isExpanded = uiState.isSidebarFocused,
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
                                                voiceLanguage = uiState.voiceLanguage,
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
                                                voiceLanguage = uiState.voiceLanguage,
                                                isLiveYouTubeApi = uiState.isLiveYouTubeApi,
                                                onSetVoiceLanguage = { viewModel.setVoiceLanguage(it) }
                                            )
                                        }
                                    }
                                }
                            }
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
            if (viewModel.uiState.value.activePlayerSong != null) {
                viewModel.closePlayer()
            }
            viewModel.openVoiceSearchModal()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}
