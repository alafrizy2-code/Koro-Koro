package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.KaraokeMicService
import com.example.audio.UsbKaraokeAudioEngine
import com.example.audio.VoiceSearchManager
import com.example.audio.VoiceSearchState
import com.example.data.local.KaraokeLibraryRepository
import com.example.data.local.KoroKoroDatabase
import com.example.data.local.PlaylistEntity
import com.example.data.local.VocalSettingsStore
import com.example.data.network.YouTubeCatalogRepository
import com.example.model.KaraokeCategory
import com.example.model.KaraokeSection
import com.example.model.KaraokeSinger
import com.example.model.KaraokeSong
import com.example.model.SidebarDestination
import com.example.model.UsbMicStatus
import com.example.model.VocalPreset
import com.example.model.VocalSettings
import com.example.player.ExternalPlayerApp
import com.example.player.ExternalPlayerManager
import com.example.player.LaunchResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MainUiState(
    val currentDestination: SidebarDestination = SidebarDestination.HOME,
    val isSidebarFocused: Boolean = true,
    val homeSections: List<KaraokeSection> = emptyList(),
    val isLoadingHome: Boolean = true,
    val isLiveYouTubeApi: Boolean = false,
    val searchQuery: String = "",
    val karaokeFilterEnabled: Boolean = true,
    val searchResults: List<KaraokeSong> = emptyList(),
    val isSearching: Boolean = false,
    val showVoiceOverlay: Boolean = false,
    val selectedCategory: KaraokeCategory? = null,
    val categorySongs: List<KaraokeSong> = emptyList(),
    val isLoadingCategory: Boolean = false,
    val selectedSongForExternal: KaraokeSong? = null,
    val externalLaunchResult: LaunchResult? = null,
    val activeInternalPlayerSong: KaraokeSong? = null,
    val relatedQueue: List<KaraokeSong> = emptyList(),
    val presetHudMessage: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val appContext = application.applicationContext
    private val database = KoroKoroDatabase.getInstance(application)
    private val libraryRepo = KaraokeLibraryRepository(database.karaokeDao())
    private val settingsStore = VocalSettingsStore(application)
    private val catalogRepo = YouTubeCatalogRepository()

    val usbAudioEngine = UsbKaraokeAudioEngine(application)
    val voiceSearchManager = VoiceSearchManager(application, usbAudioEngine)
    val externalPlayerManager = ExternalPlayerManager(application)

    private val _uiState = MutableStateFlow(
        MainUiState(isLiveYouTubeApi = catalogRepo.isLiveApiConfigured)
    )
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    val vocalSettings: StateFlow<VocalSettings> = settingsStore.vocalSettingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), VocalSettings())

    val voiceLanguage: StateFlow<String> = settingsStore.voiceLanguageFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "id-ID")

    val usbMicStatus: StateFlow<UsbMicStatus> = usbAudioEngine.status
    val voiceSearchState: StateFlow<VoiceSearchState> = voiceSearchManager.state

    val favorites: StateFlow<List<KaraokeSong>> = libraryRepo.favorites
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val history: StateFlow<List<KaraokeSong>> = libraryRepo.history
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playlists: StateFlow<List<PlaylistEntity>> = libraryRepo.playlists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val singers: List<KaraokeSinger> = catalogRepo.getSingers()
    val categories: List<KaraokeCategory> = catalogRepo.getCategories()

    init {
        viewModelScope.launch {
            vocalSettings.collect { settings ->
                usbAudioEngine.updateSettings(settings)
            }
        }
        loadHome()
    }

    fun loadHome() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingHome = true) }
            val sections = catalogRepo.loadHomeSections()
            _uiState.update {
                it.copy(
                    homeSections = sections,
                    isLoadingHome = false,
                    isLiveYouTubeApi = catalogRepo.isLiveApiConfigured
                )
            }
        }
    }

    fun setSidebarFocused(focused: Boolean) {
        _uiState.update { it.copy(isSidebarFocused = focused) }
    }

    fun navigateTo(destination: SidebarDestination) {
        if (destination == SidebarDestination.VOICE_SEARCH) {
            _uiState.update {
                it.copy(
                    currentDestination = SidebarDestination.SEARCH,
                    showVoiceOverlay = true
                )
            }
            return
        }
        _uiState.update {
            it.copy(
                currentDestination = destination,
                showVoiceOverlay = false
            )
        }
        if (destination == SidebarDestination.CATEGORIES && _uiState.value.selectedCategory == null) {
            categories.firstOrNull()?.let { selectCategory(it) }
        }
        if (destination == SidebarDestination.SEARCH && _uiState.value.searchResults.isEmpty()) {
            performSearch(_uiState.value.searchQuery)
        }
    }

    fun openVoiceSearchModal() {
        _uiState.update {
            it.copy(
                currentDestination = SidebarDestination.SEARCH,
                showVoiceOverlay = true
            )
        }
    }

    fun closeVoiceSearchModal() {
        voiceSearchManager.resetState()
        _uiState.update { it.copy(showVoiceOverlay = false) }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun toggleKaraokeFilter() {
        val next = !_uiState.value.karaokeFilterEnabled
        _uiState.update { it.copy(karaokeFilterEnabled = next) }
        performSearch(_uiState.value.searchQuery)
    }

    fun performSearch(query: String = _uiState.value.searchQuery) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    searchQuery = query,
                    isSearching = true,
                    currentDestination = SidebarDestination.SEARCH,
                    showVoiceOverlay = false
                )
            }
            val results = catalogRepo.searchKaraoke(
                rawQuery = query,
                karaokeFilterEnabled = _uiState.value.karaokeFilterEnabled
            ).getOrDefault(emptyList())
            _uiState.update {
                it.copy(
                    searchResults = results,
                    isSearching = false
                )
            }
        }
    }

    fun searchBySinger(singer: KaraokeSinger) {
        performSearch(singer.query)
    }

    fun selectCategory(category: KaraokeCategory) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    selectedCategory = category,
                    isLoadingCategory = true
                )
            }
            val songs = catalogRepo.searchKaraoke(category.query, karaokeFilterEnabled = false)
                .getOrDefault(emptyList())
            _uiState.update {
                it.copy(
                    categorySongs = songs,
                    isLoadingCategory = false
                )
            }
        }
    }

    /**
     * Primary playback flow:
     * 1. Save video info to History
     * 2. Start USB Mic & Vocal Effects foreground service
     * 3. Check TizenTube Cobalt availability & launch via Intent
     * 4. Show status / installation guide / alternative players if needed
     */
    fun playSong(song: KaraokeSong, queue: List<KaraokeSong> = emptyList()) {
        viewModelScope.launch {
            libraryRepo.recordHistory(song)
            usbAudioEngine.startEngine()
            KaraokeMicService.start(appContext)

            val fallbackQueue = queue.ifEmpty {
                _uiState.value.homeSections.firstOrNull()?.songs.orEmpty()
            }
            val result = externalPlayerManager.launchInTizenTubeCobalt(song.videoId)

            _uiState.update {
                it.copy(
                    selectedSongForExternal = song,
                    externalLaunchResult = result,
                    relatedQueue = fallbackQueue.filter { item -> item.videoId != song.videoId }
                )
            }
        }
    }

    fun retryTizenTubeForSelected() {
        val song = _uiState.value.selectedSongForExternal ?: return
        val result = externalPlayerManager.launchInTizenTubeCobalt(song.videoId)
        _uiState.update { it.copy(externalLaunchResult = result) }
    }

    fun launchAlternativePlayer(app: ExternalPlayerApp) {
        val song = _uiState.value.selectedSongForExternal ?: return
        val ok = externalPlayerManager.launchInSpecificPlayer(song.videoId, app.packageName)
        if (!ok) {
            externalPlayerManager.launchWithSystemChooser(song.videoId)
        }
    }

    fun launchSystemChooser() {
        val song = _uiState.value.selectedSongForExternal ?: return
        externalPlayerManager.launchWithSystemChooser(song.videoId)
    }

    fun openInternalPlayerFallback() {
        val song = _uiState.value.selectedSongForExternal ?: return
        _uiState.update {
            it.copy(
                selectedSongForExternal = null,
                externalLaunchResult = null,
                activeInternalPlayerSong = song
            )
        }
    }

    fun dismissExternalDialog() {
        _uiState.update {
            it.copy(
                selectedSongForExternal = null,
                externalLaunchResult = null
            )
        }
    }

    fun closeInternalPlayer() {
        _uiState.update { it.copy(activeInternalPlayerSong = null) }
    }

    fun updatePlaybackPosition(videoId: String, positionSeconds: Int) {
        viewModelScope.launch {
            libraryRepo.updatePlaybackPosition(videoId, positionSeconds)
        }
    }

    fun isFavorite(videoId: String): Flow<Boolean> = libraryRepo.isFavorite(videoId)

    fun toggleFavorite(song: KaraokeSong, currentlyFavorite: Boolean) {
        viewModelScope.launch {
            libraryRepo.toggleFavorite(song, currentlyFavorite)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            libraryRepo.clearHistory()
        }
    }

    fun createPlaylist(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            libraryRepo.createPlaylist(name)
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            libraryRepo.deletePlaylist(playlistId)
        }
    }

    fun getSongsInPlaylist(playlistId: Long): Flow<List<KaraokeSong>> =
        libraryRepo.getSongsInPlaylist(playlistId)

    fun addSongToPlaylist(playlistId: Long, song: KaraokeSong) {
        viewModelScope.launch {
            libraryRepo.addSongToPlaylist(playlistId, song)
        }
    }

    // Vocal Effects 0-9 & Mic Controls
    fun applyVocalPreset(preset: VocalPreset) {
        viewModelScope.launch {
            settingsStore.applyPreset(preset)
            _uiState.update {
                it.copy(presetHudMessage = "Efek Vokal [${preset.keyNumber}]: ${preset.displayName}")
            }
        }
    }

    fun clearPresetHud() {
        _uiState.update { it.copy(presetHudMessage = null) }
    }

    fun updateVocalSettings(transform: (VocalSettings) -> VocalSettings) {
        viewModelScope.launch {
            settingsStore.updateSettings(transform)
        }
    }

    fun setVoiceLanguage(langTag: String) {
        viewModelScope.launch {
            settingsStore.setVoiceLanguage(langTag)
        }
    }

    fun toggleAudioEngine() {
        if (usbAudioEngine.status.value.isEngineRunning) {
            usbAudioEngine.stopEngine()
            KaraokeMicService.stop(appContext)
        } else {
            usbAudioEngine.startEngine()
            KaraokeMicService.start(appContext)
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceSearchManager.resetState()
        usbAudioEngine.release()
    }
}
