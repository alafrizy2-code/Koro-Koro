package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.KaraokeSong

@Entity(tableName = "favorite_songs")
data class FavoriteSongEntity(
    @PrimaryKey val videoId: String,
    val title: String,
    val singer: String,
    val durationFormatted: String,
    val durationSeconds: Int,
    val thumbnailUrl: String,
    val category: String,
    val addedAt: Long = System.currentTimeMillis()
) {
    fun toKaraokeSong() = KaraokeSong(
        videoId = videoId,
        title = title,
        singer = singer,
        durationFormatted = durationFormatted,
        durationSeconds = durationSeconds,
        thumbnailUrl = thumbnailUrl,
        category = category
    )
}

@Entity(tableName = "history_songs")
data class HistorySongEntity(
    @PrimaryKey val videoId: String,
    val title: String,
    val singer: String,
    val durationFormatted: String,
    val durationSeconds: Int,
    val thumbnailUrl: String,
    val category: String,
    val lastPositionSeconds: Int = 0,
    val playedAt: Long = System.currentTimeMillis()
) {
    fun toKaraokeSong() = KaraokeSong(
        videoId = videoId,
        title = title,
        singer = singer,
        durationFormatted = durationFormatted,
        durationSeconds = durationSeconds,
        thumbnailUrl = thumbnailUrl,
        category = category,
        lastPositionSeconds = lastPositionSeconds
    )
}

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "playlist_songs",
    primaryKeys = ["playlistId", "videoId"]
)
data class PlaylistSongEntity(
    val playlistId: Long,
    val videoId: String,
    val title: String,
    val singer: String,
    val durationFormatted: String,
    val durationSeconds: Int,
    val thumbnailUrl: String,
    val category: String,
    val addedAt: Long = System.currentTimeMillis()
) {
    fun toKaraokeSong() = KaraokeSong(
        videoId = videoId,
        title = title,
        singer = singer,
        durationFormatted = durationFormatted,
        durationSeconds = durationSeconds,
        thumbnailUrl = thumbnailUrl,
        category = category
    )
}
