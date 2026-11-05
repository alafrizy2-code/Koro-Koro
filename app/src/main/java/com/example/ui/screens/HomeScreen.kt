package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.KaraokeSection
import com.example.model.KaraokeSinger
import com.example.model.KaraokeSong
import com.example.ui.components.KaraokeVideoCard
import com.example.ui.components.TvFocusableSurface
import com.example.ui.theme.KaraokeCrimson
import com.example.ui.theme.KaraokeCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TvBackground
import com.example.ui.theme.TvSurfaceElevated

@Composable
fun HomeScreen(
    sections: List<KaraokeSection>,
    history: List<KaraokeSong>,
    favoriteIds: Set<String>,
    singers: List<KaraokeSinger>,
    isLoading: Boolean,
    isLiveYouTubeApi: Boolean,
    onPlaySong: (KaraokeSong, List<KaraokeSong>) -> Unit,
    onSelectSinger: (KaraokeSinger) -> Unit,
    onOpenVoiceSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isLoading && sections.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = KaraokeCrimson)
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Memuat katalog karaoke...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary
                )
            }
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(26.dp)
    ) {
        // Atmospheric Koro Koro TV Stage Hero Banner (No ads / no movie promos)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp)
                    .height(190.dp)
                    .clip(RoundedCornerShape(20.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_karaoke_stage_bg_1793879349541),
                    contentDescription = "Koro Koro TV Stage",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    TvBackground.copy(alpha = 0.94f),
                                    TvBackground.copy(alpha = 0.72f),
                                    Color.Transparent
                                )
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(28.dp)
                        .fillMaxWidth(0.68f)
                ) {
                    Text(
                        text = "PANGGUNG KARAOKE PRIBADI",
                        style = MaterialTheme.typography.labelMedium,
                        color = KaraokeCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Nyanyi Sepuasnya di Layar TV",
                        style = MaterialTheme.typography.headlineLarge,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isLiveYouTubeApi) {
                            "Terhubung ke YouTube Data API v3 • Efek vokal USB Mic real-time siap"
                        } else {
                            "Katalog karaoke siap pakai • Colokkan USB Mic untuk efek vokal real-time"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        val firstSong = history.firstOrNull() ?: sections.firstOrNull()?.songs?.firstOrNull()
                        if (firstSong != null) {
                            TvFocusableSurface(
                                onClick = { onPlaySong(firstSong, sections.firstOrNull()?.songs.orEmpty()) },
                                testTag = "hero_play_button",
                                shape = RoundedCornerShape(24.dp),
                                containerColor = KaraokeCrimson,
                                focusedContainerColor = KaraokeCrimson,
                                focusedBorderColor = Color.White
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.PlayArrow,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (history.isNotEmpty()) "Lanjutkan Nyanyi" else "Mulai Karaoke",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        TvFocusableSurface(
                            onClick = onOpenVoiceSearch,
                            testTag = "hero_voice_button",
                            shape = RoundedCornerShape(24.dp),
                            containerColor = TvSurfaceElevated.copy(alpha = 0.9f),
                            focusedContainerColor = KaraokeCyan,
                            focusedBorderColor = Color.White
                        ) { isFocused ->
                            Row(
                                modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Mic,
                                    contentDescription = null,
                                    tint = if (isFocused) TvBackground else KaraokeCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Cari Lagu via Suara",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (isFocused) TvBackground else TextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // 1. Lanjutkan Terakhir Diputar (Continue Playing)
        if (history.isNotEmpty()) {
            item {
                KaraokeRowSection(
                    title = "Lanjutkan Terakhir Diputar",
                    subtitle = "Lagu yang terakhir kali kamu nyanyikan",
                    songs = history,
                    favoriteIds = favoriteIds,
                    onPlaySong = { song -> onPlaySong(song, history) }
                )
            }
        }

        // 2..9. All 8 YouTube / Curated Karaoke Sections
        items(sections, key = { it.id }) { section ->
            KaraokeRowSection(
                title = section.title,
                subtitle = section.subtitle,
                songs = section.songs,
                favoriteIds = favoriteIds,
                onPlaySong = { song -> onPlaySong(song, section.songs) }
            )
        }

        // 10. Karaoke Berdasarkan Penyanyi
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(horizontal = 28.dp)) {
                    Text(
                        text = "Karaoke Berdasarkan Penyanyi",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = "Pilih penyanyi favoritmu untuk langsung mencari lagu karaokenya",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 28.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(singers, key = { it.name }) { singer ->
                        SingerCard(
                            singer = singer,
                            onClick = { onSelectSinger(singer) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun KaraokeRowSection(
    title: String,
    subtitle: String,
    songs: List<KaraokeSong>,
    favoriteIds: Set<String>,
    onPlaySong: (KaraokeSong) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 28.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 28.dp),
            horizontalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            items(songs, key = { "${title}_${it.videoId}" }) { song ->
                KaraokeVideoCard(
                    song = song,
                    isFavorite = favoriteIds.contains(song.videoId),
                    onClick = { onPlaySong(song) }
                )
            }
        }
    }
}

@Composable
private fun SingerCard(
    singer: KaraokeSinger,
    onClick: () -> Unit
) {
    val accent = Color(singer.accentColorHex)
    TvFocusableSurface(
        onClick = onClick,
        testTag = "singer_card_${singer.name}",
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.width(184.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(accent, accent.copy(alpha = 0.45f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = singer.name,
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = singer.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = singer.genre,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
