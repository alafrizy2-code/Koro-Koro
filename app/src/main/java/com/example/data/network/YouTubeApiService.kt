package com.example.data.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Query

interface YouTubeApiService {

    @GET("search")
    suspend fun searchVideos(
        @Query("part") part: String = "snippet",
        @Query("q") query: String,
        @Query("type") type: String = "video",
        @Query("videoEmbeddable") videoEmbeddable: String = "true",
        @Query("maxResults") maxResults: Int = 15,
        @Query("order") order: String = "relevance",
        @Query("regionCode") regionCode: String = "ID",
        @Query("key") apiKey: String
    ): YouTubeSearchResponse

    @GET("videos")
    suspend fun getVideoDetails(
        @Query("part") part: String = "contentDetails,snippet",
        @Query("id") videoIds: String,
        @Query("key") apiKey: String
    ): YouTubeVideoDetailsResponse
}

@JsonClass(generateAdapter = true)
data class YouTubeSearchResponse(
    @Json(name = "items") val items: List<YouTubeSearchItem> = emptyList()
)

@JsonClass(generateAdapter = true)
data class YouTubeSearchItem(
    @Json(name = "id") val id: YouTubeVideoId,
    @Json(name = "snippet") val snippet: YouTubeSnippet
)

@JsonClass(generateAdapter = true)
data class YouTubeVideoId(
    @Json(name = "videoId") val videoId: String? = null
)

@JsonClass(generateAdapter = true)
data class YouTubeSnippet(
    @Json(name = "title") val title: String = "",
    @Json(name = "channelTitle") val channelTitle: String = "",
    @Json(name = "thumbnails") val thumbnails: YouTubeThumbnails? = null
)

@JsonClass(generateAdapter = true)
data class YouTubeThumbnails(
    @Json(name = "high") val high: YouTubeThumbnail? = null,
    @Json(name = "medium") val medium: YouTubeThumbnail? = null,
    @Json(name = "default") val defaultThumb: YouTubeThumbnail? = null
)

@JsonClass(generateAdapter = true)
data class YouTubeThumbnail(
    @Json(name = "url") val url: String = ""
)

@JsonClass(generateAdapter = true)
data class YouTubeVideoDetailsResponse(
    @Json(name = "items") val items: List<YouTubeVideoDetailItem> = emptyList()
)

@JsonClass(generateAdapter = true)
data class YouTubeVideoDetailItem(
    @Json(name = "id") val id: String = "",
    @Json(name = "contentDetails") val contentDetails: YouTubeContentDetails? = null
)

@JsonClass(generateAdapter = true)
data class YouTubeContentDetails(
    @Json(name = "duration") val duration: String = ""
)
