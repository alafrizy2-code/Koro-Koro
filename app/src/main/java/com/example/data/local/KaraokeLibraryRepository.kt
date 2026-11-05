package com.example.data.local

import com.example.model.KaraokeSong
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class KaraokeLibraryRepository(private val dao: KaraokeDao) {

    val favorites: Flow<List<KaraokeSong>> = dao.getAllFavorites().map { list ->
        list.map { it.toKaraokeSong() }
    }

    val history: Flow<List<KaraokeSong>> = dao.getHistory().map { list ->
        list.map { it.toKaraokeSong() }
    }

    val playlists: Flow<List<PlaylistEntity>> = dao.getAllPlaylists()

    fun isFavorite(videoId: String): Flow<Boolean> = dao.isFavorite(videoId)

    suspend fun toggleFavorite(song: KaraokeSong, currentlyFavorite: Boolean) {
        if (currentlyFavorite) {
            dao.deleteFavorite(song.videoId)
        } else {
            dao.insertFavorite(
                FavoriteSongEntity(
                    videoId = song.videoId,
                    title = song.title,
                    singer = song.singer,
                    durationFormatted = song.durationFormatted,
                    durationSeconds = song.durationSeconds,
                    thumbnailUrl = song.thumbnailUrl,
                    category = song.category
                )
            )
        }
    }

    suspend fun recordHistory(song: KaraokeSong) {
        dao.upsertHistory(
            HistorySongEntity(
                videoId = song.videoId,
                title = song.title,
                singer = song.singer,
                durationFormatted = song.durationFormatted,
                durationSeconds = song.durationSeconds,
                thumbnailUrl = song.thumbnailUrl,
                category = song.category,
                lastPositionSeconds = song.lastPositionSeconds
            )
        )
    }

    suspend fun updatePlaybackPosition(videoId: String, positionSeconds: Int) {
        dao.updateHistoryPosition(videoId, positionSeconds)
    }

    suspend fun clearHistory() = dao.clearHistory()

    suspend fun createPlaylist(name: String): Long {
        return dao.insertPlaylist(PlaylistEntity(name = name.trim()))
    }

    suspend fun deletePlaylist(playlistId: Long) {
        dao.deletePlaylistSongs(playlistId)
        dao.deletePlaylist(playlistId)
    }

    fun getSongsInPlaylist(playlistId: Long): Flow<List<KaraokeSong>> {
        return dao.getSongsInPlaylist(playlistId).map { list ->
            list.map { it.toKaraokeSong() }
        }
    }

    suspend fun addSongToPlaylist(playlistId: Long, song: KaraokeSong) {
        dao.addSongToPlaylist(
            PlaylistSongEntity(
                playlistId = playlistId,
                videoId = song.videoId,
                title = song.title,
                singer = song.singer,
                durationFormatted = song.durationFormatted,
                durationSeconds = song.durationSeconds,
                thumbnailUrl = song.thumbnailUrl,
                category = song.category
            )
        )
    }

    suspend fun removeSongFromPlaylist(playlistId: Long, videoId: String) {
        dao.removeSongFromPlaylist(playlistId, videoId)
    }
}
