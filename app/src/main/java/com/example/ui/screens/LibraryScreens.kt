package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.PlaylistEntity
import com.example.model.KaraokeSong
import com.example.ui.components.KaraokeVideoCard
import com.example.ui.components.TvFocusableSurface
import com.example.ui.theme.DividerSubtle
import com.example.ui.theme.KaraokeCrimson
import com.example.ui.theme.KaraokeCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TvSurface
import com.example.ui.theme.TvSurfaceElevated
import kotlinx.coroutines.flow.Flow

@Composable
fun FavoritesScreen(
    favorites: List<KaraokeSong>,
    onPlaySong: (KaraokeSong, List<KaraokeSong>) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp, vertical = 12.dp)
    ) {
        Text(
            text = "Lagu Favorit",
            style = MaterialTheme.typography.headlineLarge,
            color = TextPrimary
        )
        Text(
            text = "Koleksi lagu karaoke andalanmu",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(20.dp))

        if (favorites.isEmpty()) {
            EmptyLibraryState(
                icon = Icons.Filled.FavoriteBorder,
                title = "Belum ada lagu favorit",
                subtitle = "Tekan tombol Favorit saat memutar lagu karaoke untuk menyimpannya di sini."
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 260.dp),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                items(favorites, key = { it.videoId }) { song ->
                    KaraokeVideoCard(
                        song = song,
                        isFavorite = true,
                        cardWidth = 280.dp,
                        onClick = { onPlaySong(song, favorites) }
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryScreen(
    history: List<KaraokeSong>,
    favoriteIds: Set<String>,
    onPlaySong: (KaraokeSong, List<KaraokeSong>) -> Unit,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Riwayat Karaoke",
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextPrimary
                )
                Text(
                    text = "Daftar lagu yang baru saja kamu putar",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
            if (history.isNotEmpty()) {
                TvFocusableSurface(
                    onClick = onClearHistory,
                    testTag = "clear_history_button",
                    shape = RoundedCornerShape(16.dp),
                    containerColor = TvSurfaceElevated,
                    focusedContainerColor = KaraokeCrimson
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DeleteOutline,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Hapus Riwayat",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))

        if (history.isEmpty()) {
            EmptyLibraryState(
                icon = Icons.Filled.History,
                title = "Riwayat masih kosong",
                subtitle = "Lagu yang kamu putar akan otomatis muncul di sini."
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 260.dp),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                items(history, key = { it.videoId }) { song ->
                    KaraokeVideoCard(
                        song = song,
                        isFavorite = favoriteIds.contains(song.videoId),
                        cardWidth = 280.dp,
                        onClick = { onPlaySong(song, history) }
                    )
                }
            }
        }
    }
}

@Composable
fun PlaylistsScreen(
    playlists: List<PlaylistEntity>,
    favoriteIds: Set<String>,
    getSongsInPlaylist: (Long) -> Flow<List<KaraokeSong>>,
    onCreatePlaylist: (String) -> Unit,
    onDeletePlaylist: (Long) -> Unit,
    onPlaySong: (KaraokeSong, List<KaraokeSong>) -> Unit,
    modifier: Modifier = Modifier
) {
    var newPlaylistName by remember { mutableStateOf("") }
    var selectedPlaylistId by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(playlists) {
        if (selectedPlaylistId == null && playlists.isNotEmpty()) {
            selectedPlaylistId = playlists.first().id
        } else if (playlists.none { it.id == selectedPlaylistId }) {
            selectedPlaylistId = playlists.firstOrNull()?.id
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp, vertical = 12.dp)
    ) {
        Text(
            text = "Playlist Karaoke",
            style = MaterialTheme.typography.headlineLarge,
            color = TextPrimary
        )
        Text(
            text = "Buat daftar putar khusus untuk pesta, keluarga, atau latihan vokal",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Create Playlist Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = newPlaylistName,
                onValueChange = { newPlaylistName = it },
                placeholder = { Text("Nama playlist baru (misal: Malam Minggu)...", color = TextSecondary) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = TvSurface,
                    unfocusedContainerColor = TvSurface,
                    focusedBorderColor = KaraokeCrimson,
                    unfocusedBorderColor = DividerSubtle
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("new_playlist_input")
            )

            TvFocusableSurface(
                onClick = {
                    if (newPlaylistName.isNotBlank()) {
                        onCreatePlaylist(newPlaylistName)
                        newPlaylistName = ""
                    }
                },
                testTag = "create_playlist_button",
                shape = RoundedCornerShape(14.dp),
                containerColor = KaraokeCrimson,
                focusedContainerColor = KaraokeCrimson,
                modifier = Modifier.height(56.dp)
            ) {
                Row(
                    modifier = Modifier
                        .height(56.dp)
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Buat Playlist",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        if (playlists.isEmpty()) {
            EmptyLibraryState(
                icon = Icons.Filled.QueueMusic,
                title = "Belum ada playlist",
                subtitle = "Buat playlist pertamamu di atas, lalu tambahkan lagu dari pemutar karaoke."
            )
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(playlists, key = { it.id }) { pl ->
                    val isSelected = pl.id == selectedPlaylistId
                    TvFocusableSurface(
                        onClick = { selectedPlaylistId = pl.id },
                        shape = RoundedCornerShape(16.dp),
                        containerColor = if (isSelected) KaraokeCrimson else TvSurfaceElevated,
                        focusedContainerColor = KaraokeCrimson
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.QueueMusic,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = pl.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            val activeId = selectedPlaylistId
            if (activeId != null) {
                val songs by getSongsInPlaylist(activeId).collectAsState(initial = emptyList())
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${songs.size} Lagu dalam Playlist",
                        style = MaterialTheme.typography.titleMedium,
                        color = KaraokeCyan
                    )
                    TvFocusableSurface(
                        onClick = { onDeletePlaylist(activeId) },
                        shape = RoundedCornerShape(12.dp),
                        containerColor = TvSurfaceElevated,
                        focusedContainerColor = KaraokeCrimson
                    ) {
                        Text(
                            text = "Hapus Playlist",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                if (songs.isEmpty()) {
                    EmptyLibraryState(
                        icon = Icons.Filled.QueueMusic,
                        title = "Playlist ini masih kosong",
                        subtitle = "Tambahkan lagu ke playlist ini saat memutar video karaoke."
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 260.dp),
                        horizontalArrangement = Arrangement.spacedBy(18.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        items(songs, key = { it.videoId }) { song ->
                            KaraokeVideoCard(
                                song = song,
                                isFavorite = favoriteIds.contains(song.videoId),
                                cardWidth = 280.dp,
                                onClick = { onPlaySong(song, songs) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyLibraryState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(52.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}
