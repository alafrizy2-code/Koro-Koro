package com.example.model

data class KaraokeSong(
    val videoId: String,
    val title: String,
    val singer: String,
    val durationFormatted: String = "",
    val durationSeconds: Int = 0,
    val thumbnailUrl: String = "https://i.ytimg.com/vi/$videoId/hqdefault.jpg",
    val category: String = "Karaoke",
    val lastPositionSeconds: Int = 0
)

data class KaraokeSection(
    val id: String,
    val title: String,
    val subtitle: String,
    val songs: List<KaraokeSong>
)

data class KaraokeSinger(
    val name: String,
    val genre: String,
    val query: String,
    val accentColorHex: Long
)

data class KaraokeCategory(
    val id: String,
    val title: String,
    val subtitle: String,
    val query: String,
    val accentColorHex: Long
)
