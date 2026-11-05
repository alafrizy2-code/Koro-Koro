package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CastConnected
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.KaraokeSong
import com.example.model.VocalPreset
import com.example.player.ExternalPlayerApp
import com.example.player.LaunchResult
import com.example.ui.theme.KaraokeCrimson
import com.example.ui.theme.KaraokeCyan
import com.example.ui.theme.KaraokeEmerald
import com.example.ui.theme.KaraokeGold
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TvBackground
import com.example.ui.theme.TvSurface
import com.example.ui.theme.TvSurfaceElevated

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ExternalPlayerDialog(
    song: KaraokeSong,
    launchResult: LaunchResult,
    isFavorite: Boolean,
    activePreset: VocalPreset,
    onRetryTizenTube: () -> Unit,
    onSelectAlternativePlayer: (ExternalPlayerApp) -> Unit,
    onOpenSystemChooser: () -> Unit,
    onOpenInternalPlayer: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDismiss: () -> Unit
) {
    val primaryFocusRequester = remember { FocusRequester() }

    BackHandler {
        onDismiss()
    }

    LaunchedEffect(launchResult) {
        try {
            primaryFocusRequester.requestFocus()
        } catch (_: Exception) {
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.76f)
                .clip(RoundedCornerShape(24.dp))
                .background(TvSurface)
                .border(
                    width = 1.5.dp,
                    color = when (launchResult) {
                        is LaunchResult.LaunchedTizenTube -> KaraokeEmerald
                        is LaunchResult.TizenTubeNotInstalled -> KaraokeGold
                        is LaunchResult.LaunchFailed -> KaraokeCrimson
                    },
                    shape = RoundedCornerShape(24.dp)
                )
                .focusGroup()
                .focusProperties { exit = { FocusRequester.Cancel } }
                .padding(28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (launchResult) {
                is LaunchResult.LaunchedTizenTube -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.CastConnected,
                            contentDescription = null,
                            tint = KaraokeEmerald,
                            modifier = Modifier.size(30.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Diputar melalui ${launchResult.appName}",
                                style = MaterialTheme.typography.headlineMedium,
                                color = KaraokeEmerald
                            )
                            Text(
                                text = "Efek Vokal [${activePreset.keyNumber}] ${activePreset.displayName} aktif di latar belakang",
                                style = MaterialTheme.typography.labelMedium,
                                color = KaraokeCyan
                            )
                        }
                    }

                    Text(
                        text = "${song.title} — ${song.singer}",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TvFocusableSurface(
                            onClick = onRetryTizenTube,
                            shape = RoundedCornerShape(20.dp),
                            containerColor = KaraokeCrimson,
                            focusedContainerColor = KaraokeCrimson,
                            modifier = Modifier.focusRequester(primaryFocusRequester)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.OpenInNew,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Buka Lagi di TizenTube",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.White
                                )
                            }
                        }

                        TvFocusableSurface(
                            onClick = onToggleFavorite,
                            shape = RoundedCornerShape(20.dp),
                            containerColor = TvSurfaceElevated,
                            focusedContainerColor = KaraokeCyan
                        ) { isFocused ->
                            Row(
                                modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                    contentDescription = null,
                                    tint = if (isFocused) TvBackground else if (isFavorite) KaraokeCrimson else Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isFavorite) "Difavoritkan" else "Simpan Favorit",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (isFocused) TvBackground else TextPrimary
                                )
                            }
                        }

                        TvFocusableSurface(
                            onClick = onOpenInternalPlayer,
                            shape = RoundedCornerShape(20.dp),
                            containerColor = TvSurfaceElevated,
                            focusedContainerColor = KaraokeCyan
                        ) { isFocused ->
                            Text(
                                text = "Pemutar Bawaan",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isFocused) TvBackground else TextPrimary,
                                modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp)
                            )
                        }

                        TvFocusableSurface(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(20.dp),
                            containerColor = TvSurfaceElevated,
                            focusedContainerColor = KaraokeCrimson
                        ) {
                            Text(
                                text = "Kembali ke Katalog",
                                style = MaterialTheme.typography.labelLarge,
                                color = TextPrimary,
                                modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp)
                            )
                        }
                    }
                }

                is LaunchResult.TizenTubeNotInstalled -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.ErrorOutline,
                            contentDescription = null,
                            tint = KaraokeGold,
                            modifier = Modifier.size(30.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "TizenTube Cobalt Belum Terpasang",
                            style = MaterialTheme.typography.headlineMedium,
                            color = KaraokeGold
                        )
                    }

                    Text(
                        text = "Petunjuk Instalasi TizenTube Cobalt di Android TV Box B866F:\n" +
                            "1. Unduh APK resmi TizenTube Cobalt dari GitHub (reisxd/TizenTubeCobalt/releases).\n" +
                            "2. Instal APK tersebut di perangkat Android TV Anda.\n" +
                            "3. Setelah terpasang, Koro Koro TV otomatis membuka semua video karaoke melalui TizenTube Cobalt.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )

                    AlternativePlayersSection(
                        alternatives = launchResult.alternatives,
                        primaryFocusRequester = primaryFocusRequester,
                        onSelectAlternativePlayer = onSelectAlternativePlayer,
                        onOpenSystemChooser = onOpenSystemChooser,
                        onOpenInternalPlayer = onOpenInternalPlayer,
                        onDismiss = onDismiss
                    )
                }

                is LaunchResult.LaunchFailed -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.ErrorOutline,
                            contentDescription = null,
                            tint = KaraokeCrimson,
                            modifier = Modifier.size(30.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Gagal Membuka TizenTube Cobalt",
                            style = MaterialTheme.typography.headlineMedium,
                            color = KaraokeCrimson
                        )
                    }

                    Text(
                        text = "Keterangan: ${launchResult.reason}. Pilih pemutar alternatif di bawah ini:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )

                    AlternativePlayersSection(
                        alternatives = launchResult.alternatives,
                        primaryFocusRequester = primaryFocusRequester,
                        onSelectAlternativePlayer = onSelectAlternativePlayer,
                        onOpenSystemChooser = onOpenSystemChooser,
                        onOpenInternalPlayer = onOpenInternalPlayer,
                        onDismiss = onDismiss
                    )
                }
            }
        }
    }
}

@Composable
private fun AlternativePlayersSection(
    alternatives: List<ExternalPlayerApp>,
    primaryFocusRequester: FocusRequester,
    onSelectAlternativePlayer: (ExternalPlayerApp) -> Unit,
    onOpenSystemChooser: () -> Unit,
    onOpenInternalPlayer: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Pilih Pemutar Alternatif:",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                TvFocusableSurface(
                    onClick = onOpenInternalPlayer,
                    shape = RoundedCornerShape(18.dp),
                    containerColor = KaraokeCrimson,
                    focusedContainerColor = KaraokeCrimson,
                    modifier = Modifier.focusRequester(primaryFocusRequester)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
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
                            text = "Pemutar Bawaan Koro Koro",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White
                        )
                    }
                }
            }

            items(alternatives, key = { it.packageName }) { app ->
                TvFocusableSurface(
                    onClick = { onSelectAlternativePlayer(app) },
                    shape = RoundedCornerShape(18.dp),
                    containerColor = TvSurfaceElevated,
                    focusedContainerColor = KaraokeCyan
                ) { isFocused ->
                    Text(
                        text = app.appName,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isFocused) TvBackground else TextPrimary,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp)
                    )
                }
            }

            item {
                TvFocusableSurface(
                    onClick = onOpenSystemChooser,
                    shape = RoundedCornerShape(18.dp),
                    containerColor = TvSurfaceElevated,
                    focusedContainerColor = KaraokeCyan
                ) { isFocused ->
                    Text(
                        text = "Pilih Aplikasi Lain...",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isFocused) TvBackground else TextPrimary,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp)
                    )
                }
            }

            item {
                TvFocusableSurface(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(18.dp),
                    containerColor = TvBackground,
                    focusedContainerColor = KaraokeCrimson
                ) {
                    Text(
                        text = "Tutup",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp)
                    )
                }
            }
        }
    }
}
