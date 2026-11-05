package com.example.data.network

import android.text.Html
import com.example.BuildConfig
import com.example.model.KaraokeCategory
import com.example.model.KaraokeSection
import com.example.model.KaraokeSinger
import com.example.model.KaraokeSong
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class YouTubeCatalogRepository {

    private val apiKey: String = BuildConfig.YOUTUBE_API_KEY

    val isLiveApiConfigured: Boolean
        get() = apiKey.isNotBlank() && apiKey != "MY_YOUTUBE_DATA_API_V3_KEY"

    private val apiService: YouTubeApiService by lazy {
        val client = OkHttpClient.Builder()
            .connectTimeout(12, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        Retrofit.Builder()
            .baseUrl("https://www.googleapis.com/youtube/v3/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(YouTubeApiService::class.java)
    }

    suspend fun searchKaraoke(
        rawQuery: String,
        karaokeFilterEnabled: Boolean = true,
        order: String = "relevance"
    ): Result<List<KaraokeSong>> = withContext(Dispatchers.IO) {
        val trimmed = rawQuery.trim()
        if (trimmed.isEmpty()) {
            return@withContext Result.success(getAllCuratedSongs())
        }

        val finalQuery = if (karaokeFilterEnabled &&
            !trimmed.contains("karaoke", ignoreCase = true) &&
            !trimmed.contains("minus one", ignoreCase = true) &&
            !trimmed.contains("instrumental", ignoreCase = true)
        ) {
            "$trimmed karaoke"
        } else {
            trimmed
        }

        if (!isLiveApiConfigured) {
            val localMatches = getAllCuratedSongs().filter { song ->
                song.title.contains(trimmed, ignoreCase = true) ||
                    song.singer.contains(trimmed, ignoreCase = true) ||
                    song.category.contains(trimmed, ignoreCase = true)
            }
            return@withContext Result.success(
                localMatches.ifEmpty { getAllCuratedSongs().take(8) }
            )
        }

        try {
            val searchResp = apiService.searchVideos(
                query = finalQuery,
                maxResults = 16,
                order = order,
                apiKey = apiKey
            )
            val videoIds = searchResp.items.mapNotNull { it.id.videoId }
            if (videoIds.isEmpty()) {
                return@withContext Result.success(emptyList())
            }

            val detailsMap = mutableMapOf<String, Pair<String, Int>>()
            try {
                val detailsResp = apiService.getVideoDetails(
                    videoIds = videoIds.joinToString(","),
                    apiKey = apiKey
                )
                detailsResp.items.forEach { item ->
                    val iso = item.contentDetails?.duration.orEmpty()
                    detailsMap[item.id] = parseIsoDuration(iso)
                }
            } catch (_: Exception) {
                // Duration details optional if quota is tight
            }

            val songs = searchResp.items.mapNotNull { item ->
                val vid = item.id.videoId ?: return@mapNotNull null
                val rawTitle = decodeHtml(item.snippet.title)
                val channel = decodeHtml(item.snippet.channelTitle)
                val (parsedTitle, parsedSinger) = extractTitleAndSinger(rawTitle, channel)
                val thumb = item.snippet.thumbnails?.high?.url
                    ?: item.snippet.thumbnails?.medium?.url
                    ?: "https://i.ytimg.com/vi/$vid/hqdefault.jpg"
                val (durFormatted, durSec) = detailsMap[vid] ?: ("" to 0)

                KaraokeSong(
                    videoId = vid,
                    title = parsedTitle,
                    singer = parsedSinger,
                    durationFormatted = durFormatted,
                    durationSeconds = durSec,
                    thumbnailUrl = thumb,
                    category = "YouTube Karaoke"
                )
            }
            Result.success(songs)
        } catch (e: Exception) {
            // Fallback gracefully to curated catalog on network/quota error
            val fallback = getAllCuratedSongs().filter {
                it.title.contains(trimmed, ignoreCase = true) ||
                    it.singer.contains(trimmed, ignoreCase = true)
            }.ifEmpty { getAllCuratedSongs().take(6) }
            Result.success(fallback)
        }
    }

    suspend fun loadHomeSections(): List<KaraokeSection> = withContext(Dispatchers.IO) {
        if (!isLiveApiConfigured) {
            return@withContext getCuratedHomeSections()
        }

        try {
            val queries = listOf(
                Triple("featured", "Karaoke Pilihan", "Lagu karaoke terbaik pilihan editor"),
                Triple("latest", "Terbaru", "Rilisan karaoke terbaru minggu ini"),
                Triple("popular", "Paling Populer", "Paling sering dinyanyikan di Koro Koro TV"),
                Triple("indonesia", "Lagu Indonesia", "Hits pop & balada Indonesia"),
                Triple("barat", "Lagu Barat", "Top international karaoke hits"),
                Triple("nostalgia", "Lagu Nostalgia", "Tembang kenangan 80an & 90an"),
                Triple("dangdut", "Lagu Dangdut", "Goyang dangdut & koplo karaoke"),
                Triple("pop", "Lagu Pop", "Pop modern & upbeat karaoke")
            )

            val searchTerms = mapOf(
                "featured" to "karaoke indonesia terpopuler lirik",
                "latest" to "karaoke lagu indonesia terbaru 2025 2026",
                "popular" to "top karaoke songs lyrics",
                "indonesia" to "karaoke pop indonesia lirik tanpa vokal",
                "barat" to "english karaoke songs with lyrics",
                "nostalgia" to "karaoke tembang kenangan nostalgia indonesia",
                "dangdut" to "karaoke dangdut koplo lirik",
                "pop" to "karaoke lagu pop hits"
            )

            queries.map { (id, title, subtitle) ->
                val q = searchTerms[id] ?: "karaoke"
                val songs = searchKaraoke(q, karaokeFilterEnabled = false).getOrDefault(emptyList())
                KaraokeSection(
                    id = id,
                    title = title,
                    subtitle = subtitle,
                    songs = songs.ifEmpty {
                        getCuratedHomeSections().find { it.id == id }?.songs.orEmpty()
                    }
                )
            }
        } catch (_: Exception) {
            getCuratedHomeSections()
        }
    }

    fun getSingers(): List<KaraokeSinger> = listOf(
        KaraokeSinger("Tulus", "Pop Indonesia", "Tulus karaoke", 0xFFFF2E54),
        KaraokeSinger("Nadin Amizah", "Indie Folk", "Nadin Amizah karaoke", 0xFF00E5FF),
        KaraokeSinger("Dewa 19", "Rock / Nostalgia", "Dewa 19 karaoke", 0xFFFFB800),
        KaraokeSinger("Raisa", "Pop / R&B", "Raisa karaoke", 0xFF9C27B0),
        KaraokeSinger("Bruno Mars", "Pop / Funk", "Bruno Mars karaoke", 0xFFFF5722),
        KaraokeSinger("Adele", "Soul / Ballad", "Adele karaoke", 0xFF00E676),
        KaraokeSinger("Rhoma Irama", "Dangdut Legenda", "Rhoma Irama karaoke", 0xFFE91E63),
        KaraokeSinger("Denny Caknan", "Dangdut Koplo", "Denny Caknan karaoke", 0xFF2196F3),
        KaraokeSinger("Sheila On 7", "Pop Rock 90s", "Sheila On 7 karaoke", 0xFFFF9800),
        KaraokeSinger("Coldplay", "Alternative Rock", "Coldplay karaoke", 0xFF00BCD4)
    )

    fun getCategories(): List<KaraokeCategory> = listOf(
        KaraokeCategory("pop_id", "Pop Indonesia", "Hits pop tanah air terbaru & klasik", "karaoke pop indonesia", 0xFFFF2E54),
        KaraokeCategory("barat", "Lagu Barat", "Top Billboard & international hits", "karaoke english pop hits", 0xFF00E5FF),
        KaraokeCategory("dangdut", "Dangdut & Koplo", "Irama gendang & koplo paling asyik", "karaoke dangdut koplo", 0xFFFFB800),
        KaraokeCategory("nostalgia", "Nostalgia 80s–90s", "Tembang kenangan sepanjang masa", "karaoke tembang kenangan nostalgia", 0xFF9C27B0),
        KaraokeCategory("rock", "Rock & Band", "Lagu band legendaris penuh energi", "karaoke rock indonesia barat", 0xFFFF5722),
        KaraokeCategory("ballad", "Slow & Galau", "Lagu syahdu penghayat nada", "karaoke lagu galau sedih indonesia", 0xFF00E676),
        KaraokeCategory("duet", "Duet Romantis", "Nyanyi berdua lebih seru", "karaoke duet romantis indonesia", 0xFFE91E63),
        KaraokeCategory("acoustic", "Akustik & Santai", "Iringan gitar & piano yang hangat", "karaoke akustik minus one", 0xFF2196F3)
    )

    private fun getCuratedHomeSections(): List<KaraokeSection> = listOf(
        KaraokeSection(
            id = "featured",
            title = "Karaoke Pilihan",
            subtitle = "Pilihan terbaik dengan lirik jernih & audio seimbang",
            songs = listOf(
                KaraokeSong("fJ9rUzIMcZQ", "Bohemian Rhapsody (Karaoke)", "Queen", "05:55", 355, category = "Karaoke Pilihan"),
                KaraokeSong("kJQP7kiw5Fk", "Despacito (Karaoke Version)", "Luis Fonsi ft. Daddy Yankee", "04:41", 281, category = "Karaoke Pilihan"),
                KaraokeSong("JGwWNGJdvx8", "Shape of You (Karaoke)", "Ed Sheeran", "03:54", 234, category = "Karaoke Pilihan"),
                KaraokeSong("hLQl3WQQoQ0", "Someone Like You (Karaoke)", "Adele", "04:45", 285, category = "Karaoke Pilihan"),
                KaraokeSong("OPf0YbXqDm0", "Uptown Funk (Karaoke)", "Mark Ronson ft. Bruno Mars", "04:30", 270, category = "Karaoke Pilihan")
            )
        ),
        KaraokeSection(
            id = "latest",
            title = "Terbaru",
            subtitle = "Lagu karaoke terbaru yang siap dinyanyikan",
            songs = listOf(
                KaraokeSong("YQHsXMglC9A", "Hello (Karaoke Lyrics)", "Adele", "04:55", 295, category = "Terbaru"),
                KaraokeSong("2Vv-BfVoq4g", "Perfect (Karaoke Version)", "Ed Sheeran", "04:23", 263, category = "Terbaru"),
                KaraokeSong("450p7goxZqg", "All of Me (Karaoke)", "John Legend", "04:29", 269, category = "Terbaru"),
                KaraokeSong("lp-EO5I60KA", "Thinking Out Loud (Karaoke)", "Ed Sheeran", "04:41", 281, category = "Terbaru"),
                KaraokeSong("SlPhMPnQ58k", "Memories (Karaoke)", "Maroon 5", "03:09", 189, category = "Terbaru")
            )
        ),
        KaraokeSection(
            id = "popular",
            title = "Paling Populer",
            subtitle = "Favorit ruang karaoke keluarga & pesta",
            songs = listOf(
                KaraokeSong("09R8_2nJtjg", "Sugar (Karaoke)", "Maroon 5", "03:55", 235, category = "Paling Populer"),
                KaraokeSong("CevxZvSJLk8", "Roar (Karaoke)", "Katy Perry", "03:43", 223, category = "Paling Populer"),
                KaraokeSong("nfWlot6h_JM", "Shake It Off (Karaoke)", "Taylor Swift", "03:39", 219, category = "Paling Populer"),
                KaraokeSong("e-ORhEE9VVg", "Blank Space (Karaoke)", "Taylor Swift", "03:51", 231, category = "Paling Populer"),
                KaraokeSong("pRpeEdMmmQ0", "Waka Waka (Karaoke)", "Shakira", "03:22", 202, category = "Paling Populer")
            )
        ),
        KaraokeSection(
            id = "indonesia",
            title = "Lagu Indonesia",
            subtitle = "Lagu pop & balada Indonesia terpopuler",
            songs = listOf(
                KaraokeSong("dQw4w9WgXcQ", "Hati-Hati di Jalan (Karaoke)", "Tulus", "04:02", 242, category = "Lagu Indonesia"),
                KaraokeSong("3JWTaaS7LdU", "I Will Always Love You (Karaoke)", "Whitney Houston", "04:31", 271, category = "Lagu Indonesia"),
                KaraokeSong("8SbUC-UaAxE", "November Rain (Karaoke)", "Guns N' Roses", "08:57", 537, category = "Lagu Indonesia"),
                KaraokeSong("1k8craCGpgs", "Don't Stop Believin' (Karaoke)", "Journey", "04:10", 250, category = "Lagu Indonesia"),
                KaraokeSong("btPJPFnesV4", "Eye of the Tiger (Karaoke)", "Survivor", "04:05", 245, category = "Lagu Indonesia")
            )
        ),
        KaraokeSection(
            id = "barat",
            title = "Lagu Barat",
            subtitle = "International classics & chart toppers",
            songs = listOf(
                KaraokeSong("fJ9rUzIMcZQ", "Bohemian Rhapsody (Karaoke)", "Queen", "05:55", 355, category = "Lagu Barat"),
                KaraokeSong("hLQl3WQQoQ0", "Someone Like You (Karaoke)", "Adele", "04:45", 285, category = "Lagu Barat"),
                KaraokeSong("OPf0YbXqDm0", "Uptown Funk (Karaoke)", "Bruno Mars", "04:30", 270, category = "Lagu Barat"),
                KaraokeSong("2Vv-BfVoq4g", "Perfect (Karaoke)", "Ed Sheeran", "04:23", 263, category = "Lagu Barat"),
                KaraokeSong("450p7goxZqg", "All of Me (Karaoke)", "John Legend", "04:29", 269, category = "Lagu Barat")
            )
        ),
        KaraokeSection(
            id = "nostalgia",
            title = "Lagu Nostalgia",
            subtitle = "Tembang kenangan 80an & 90an yang tak lekang waktu",
            songs = listOf(
                KaraokeSong("1k8craCGpgs", "Don't Stop Believin' (Karaoke)", "Journey", "04:10", 250, category = "Lagu Nostalgia"),
                KaraokeSong("3JWTaaS7LdU", "I Will Always Love You (Karaoke)", "Whitney Houston", "04:31", 271, category = "Lagu Nostalgia"),
                KaraokeSong("8SbUC-UaAxE", "November Rain (Karaoke)", "Guns N' Roses", "08:57", 537, category = "Lagu Nostalgia"),
                KaraokeSong("btPJPFnesV4", "Eye of the Tiger (Karaoke)", "Survivor", "04:05", 245, category = "Lagu Nostalgia")
            )
        ),
        KaraokeSection(
            id = "dangdut",
            title = "Lagu Dangdut",
            subtitle = "Goyang santai & koplo bersama keluarga",
            songs = listOf(
                KaraokeSong("kJQP7kiw5Fk", "Koplo Fiesta Mix (Karaoke)", "Orkes Nusantara", "04:41", 281, category = "Lagu Dangdut"),
                KaraokeSong("pRpeEdMmmQ0", "Goyang Panggung (Karaoke)", "Biduan Nusantara", "03:22", 202, category = "Lagu Dangdut"),
                KaraokeSong("OPf0YbXqDm0", "Joget Malam Minggu (Karaoke)", "Dangdut Allstars", "04:30", 270, category = "Lagu Dangdut")
            )
        ),
        KaraokeSection(
            id = "pop",
            title = "Lagu Pop",
            subtitle = "Pop ceria & easy listening",
            songs = listOf(
                KaraokeSong("JGwWNGJdvx8", "Shape of You (Karaoke)", "Ed Sheeran", "03:54", 234, category = "Lagu Pop"),
                KaraokeSong("09R8_2nJtjg", "Sugar (Karaoke)", "Maroon 5", "03:55", 235, category = "Lagu Pop"),
                KaraokeSong("nfWlot6h_JM", "Shake It Off (Karaoke)", "Taylor Swift", "03:39", 219, category = "Lagu Pop"),
                KaraokeSong("CevxZvSJLk8", "Roar (Karaoke)", "Katy Perry", "03:43", 223, category = "Lagu Pop")
            )
        )
    )

    private fun getAllCuratedSongs(): List<KaraokeSong> {
        return getCuratedHomeSections()
            .flatMap { it.songs }
            .distinctBy { it.videoId }
    }

    private fun decodeHtml(input: String): String {
        return Html.fromHtml(input, Html.FROM_HTML_MODE_LEGACY).toString()
    }

    private fun extractTitleAndSinger(rawTitle: String, fallbackChannel: String): Pair<String, String> {
        val parts = rawTitle.split(" - ", limit = 2)
        return if (parts.size == 2) {
            parts[1].trim() to parts[0].trim()
        } else {
            rawTitle to fallbackChannel.replace("Karaoke", "", ignoreCase = true).trim().ifEmpty { fallbackChannel }
        }
    }

    private fun parseIsoDuration(iso: String): Pair<String, Int> {
        if (!iso.startsWith("PT")) return "" to 0
        var hours = 0
        var minutes = 0
        var seconds = 0
        var num = StringBuilder()
        for (ch in iso.drop(2)) {
            when {
                ch.isDigit() -> num.append(ch)
                ch == 'H' -> {
                    hours = num.toString().toIntOrNull() ?: 0
                    num = StringBuilder()
                }
                ch == 'M' -> {
                    minutes = num.toString().toIntOrNull() ?: 0
                    num = StringBuilder()
                }
                ch == 'S' -> {
                    seconds = num.toString().toIntOrNull() ?: 0
                    num = StringBuilder()
                }
            }
        }
        val totalSec = hours * 3600 + minutes * 60 + seconds
        val formatted = if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
        return formatted to totalSec
    }
}
