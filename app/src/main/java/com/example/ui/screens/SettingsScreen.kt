package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.components.TvFocusableSurface
import com.example.ui.theme.KaraokeCrimson
import com.example.ui.theme.KaraokeCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TvSurface
import com.example.ui.theme.TvSurfaceElevated

@Composable
fun SettingsScreen(
    voiceLanguage: String,
    isLiveYouTubeApi: Boolean,
    onSetVoiceLanguage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 28.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Text(
                text = "Pengaturan Koro Koro TV",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary
            )
            Text(
                text = "Atur bahasa pencarian suara dan informasi koneksi YouTube",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }

        // Voice Search Language
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(TvSurface)
                    .padding(22.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Bahasa Pencarian Suara",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Pilih bahasa utama untuk pengenalan suara melalui remote Android TV",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TvFocusableSurface(
                        onClick = { onSetVoiceLanguage("id-ID") },
                        shape = RoundedCornerShape(14.dp),
                        containerColor = if (voiceLanguage == "id-ID") KaraokeCrimson else TvSurfaceElevated
                    ) {
                        Text(
                            text = "Bahasa Indonesia",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp)
                        )
                    }
                    TvFocusableSurface(
                        onClick = { onSetVoiceLanguage("en-US") },
                        shape = RoundedCornerShape(14.dp),
                        containerColor = if (voiceLanguage == "en-US") KaraokeCrimson else TvSurfaceElevated
                    ) {
                        Text(
                            text = "English",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp)
                        )
                    }
                }
            }
        }

        // YouTube API & Ad-Block Status
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(TvSurface)
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Status Layanan & Pemutar",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
                Text(
                    text = if (isLiveYouTubeApi) {
                        "• YouTube Data API v3: Terhubung (Pencarian langsung aktif)"
                    } else {
                        "• YouTube Data API v3: Mode Katalog Bawaan (Tambahkan YOUTUBE_API_KEY di Secrets untuk pencarian YouTube live)"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = KaraokeCyan
                )
                Text(
                    text = "• Pemutar Bebas Iklan: Aktif (youtube-nocookie + filter skrip iklan)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }
    }
}
