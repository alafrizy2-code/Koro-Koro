package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.UsbMicStatus
import com.example.model.VocalPreset
import com.example.model.VocalSettings
import com.example.ui.components.TvFocusableSurface
import com.example.ui.theme.KaraokeCrimson
import com.example.ui.theme.KaraokeCyan
import com.example.ui.theme.KaraokeEmerald
import com.example.ui.theme.KaraokeGold
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TvBackground
import com.example.ui.theme.TvSurface
import com.example.ui.theme.TvSurfaceElevated
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    vocalSettings: VocalSettings,
    usbMicStatus: UsbMicStatus,
    voiceLanguage: String,
    isLiveYouTubeApi: Boolean,
    onApplyPreset: (VocalPreset) -> Unit,
    onUpdateVocalSettings: ((VocalSettings) -> VocalSettings) -> Unit,
    onToggleEngine: () -> Unit,
    onRefreshUsbMic: () -> Unit,
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
                text = "Pengaturan Mikrofon USB & Efek Vokal",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary
            )
            Text(
                text = "Atur pemrosesan suara real-time, Mic Monitor, dan diagnostik perangkat Android TV",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }

        // USB Microphone & Audio Routing Diagnostic Card
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(TvSurface)
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(
                                    if (usbMicStatus.isUsbConnected) KaraokeEmerald.copy(alpha = 0.18f)
                                    else KaraokeGold.copy(alpha = 0.18f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Usb,
                                contentDescription = "USB Microphone",
                                tint = if (usbMicStatus.isUsbConnected) KaraokeEmerald else KaraokeGold,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = usbMicStatus.deviceName,
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimary
                            )
                            Text(
                                text = "${usbMicStatus.sampleRateHz} Hz • Latensi ~${usbMicStatus.estimatedLatencyMs} ms • ${usbMicStatus.routingNote}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        TvFocusableSurface(
                            onClick = onRefreshUsbMic,
                            testTag = "refresh_usb_mic_button",
                            shape = RoundedCornerShape(14.dp),
                            containerColor = TvSurfaceElevated,
                            focusedContainerColor = KaraokeCyan
                        ) { isFocused ->
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Refresh,
                                    contentDescription = "Deteksi Ulang USB",
                                    tint = if (isFocused) TvBackground else TextPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Deteksi USB",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (isFocused) TvBackground else TextPrimary
                                )
                            }
                        }

                        TvFocusableSurface(
                            onClick = onToggleEngine,
                            testTag = "toggle_audio_engine_button",
                            shape = RoundedCornerShape(14.dp),
                            containerColor = if (usbMicStatus.isEngineRunning) KaraokeEmerald else KaraokeCrimson,
                            focusedContainerColor = KaraokeCrimson
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.GraphicEq,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (usbMicStatus.isEngineRunning) "DSP Aktif" else "Aktifkan DSP",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Level Sinyal Mikrofon",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { usbMicStatus.inputPeakLevel },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (usbMicStatus.inputPeakLevel > 0.85f) KaraokeCrimson else KaraokeCyan,
                    trackColor = TvSurfaceElevated
                )
            }
        }

        // Vocal Presets (0 - 9 Remote Mapping)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(TvSurface)
                    .padding(20.dp)
            ) {
                Text(
                    text = "Preset Efek Vokal (Tombol Angka Remote 0–9)",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
                Text(
                    text = "Saat mode karaoke layar penuh, tekan angka 0–9 pada remote TV untuk ganti efek instan",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(14.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(VocalPreset.entries, key = { it.keyNumber }) { preset ->
                        val isSelected = vocalSettings.preset == preset
                        TvFocusableSurface(
                            onClick = { onApplyPreset(preset) },
                            testTag = "preset_chip_${preset.keyNumber}",
                            shape = RoundedCornerShape(14.dp),
                            containerColor = if (isSelected) KaraokeCrimson else TvSurfaceElevated,
                            focusedContainerColor = KaraokeCrimson
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                                Text(
                                    text = "[${preset.keyNumber}] ${preset.displayName}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = preset.shortDesc,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Detailed Audio Parameters & Mic Monitor
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(TvSurface)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Kontrol Audio & Mic Monitor",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )

                // Mic Monitor ON/OFF
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(TvSurfaceElevated)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Mic Monitor (Suara Mikrofon ke Output TV)",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = if (vocalSettings.micMonitorEnabled) "ON — Suara mikrofon terdengar melalui speaker TV"
                            else "OFF — Suara mikrofon tidak dikeluarkan ke speaker TV",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (vocalSettings.micMonitorEnabled) KaraokeEmerald else TextSecondary
                        )
                    }
                    Switch(
                        checked = vocalSettings.micMonitorEnabled,
                        onCheckedChange = { enabled ->
                            onUpdateVocalSettings { it.copy(micMonitorEnabled = enabled) }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = KaraokeCrimson
                        )
                    )
                }

                AudioStepperRow(
                    label = "Mic Monitor Volume",
                    valueText = "${(vocalSettings.micMonitorVolume * 100).roundToInt()}%",
                    onDecrease = {
                        onUpdateVocalSettings {
                            it.copy(micMonitorVolume = (it.micMonitorVolume - 0.1f).coerceIn(0f, 1f))
                        }
                    },
                    onIncrease = {
                        onUpdateVocalSettings {
                            it.copy(micMonitorVolume = (it.micMonitorVolume + 0.1f).coerceIn(0f, 1f))
                        }
                    }
                )

                AudioStepperRow(
                    label = "Mic Volume (Gain)",
                    valueText = "${(vocalSettings.micVolume * 100).roundToInt()}%",
                    onDecrease = {
                        onUpdateVocalSettings {
                            it.copy(micVolume = (it.micVolume - 0.1f).coerceIn(0f, 2f))
                        }
                    },
                    onIncrease = {
                        onUpdateVocalSettings {
                            it.copy(micVolume = (it.micVolume + 0.1f).coerceIn(0f, 2f))
                        }
                    }
                )

                AudioStepperRow(
                    label = "Echo Level",
                    valueText = "${(vocalSettings.echoLevel * 100).roundToInt()}%",
                    onDecrease = {
                        onUpdateVocalSettings {
                            it.copy(
                                preset = VocalPreset.CUSTOM_EFFECT,
                                echoLevel = (it.echoLevel - 0.08f).coerceIn(0f, 1f)
                            )
                        }
                    },
                    onIncrease = {
                        onUpdateVocalSettings {
                            it.copy(
                                preset = VocalPreset.CUSTOM_EFFECT,
                                echoLevel = (it.echoLevel + 0.08f).coerceIn(0f, 1f)
                            )
                        }
                    }
                )

                AudioStepperRow(
                    label = "Reverb",
                    valueText = "${(vocalSettings.reverbLevel * 100).roundToInt()}%",
                    onDecrease = {
                        onUpdateVocalSettings {
                            it.copy(
                                preset = VocalPreset.CUSTOM_EFFECT,
                                reverbLevel = (it.reverbLevel - 0.08f).coerceIn(0f, 1f)
                            )
                        }
                    },
                    onIncrease = {
                        onUpdateVocalSettings {
                            it.copy(
                                preset = VocalPreset.CUSTOM_EFFECT,
                                reverbLevel = (it.reverbLevel + 0.08f).coerceIn(0f, 1f)
                            )
                        }
                    }
                )

                AudioStepperRow(
                    label = "Bass EQ",
                    valueText = "${if (vocalSettings.bassDb >= 0) "+" else ""}${vocalSettings.bassDb.roundToInt()} dB",
                    onDecrease = {
                        onUpdateVocalSettings {
                            it.copy(
                                preset = VocalPreset.CUSTOM_EFFECT,
                                bassDb = (it.bassDb - 1.5f).coerceIn(-12f, 12f)
                            )
                        }
                    },
                    onIncrease = {
                        onUpdateVocalSettings {
                            it.copy(
                                preset = VocalPreset.CUSTOM_EFFECT,
                                bassDb = (it.bassDb + 1.5f).coerceIn(-12f, 12f)
                            )
                        }
                    }
                )

                AudioStepperRow(
                    label = "Treble EQ",
                    valueText = "${if (vocalSettings.trebleDb >= 0) "+" else ""}${vocalSettings.trebleDb.roundToInt()} dB",
                    onDecrease = {
                        onUpdateVocalSettings {
                            it.copy(
                                preset = VocalPreset.CUSTOM_EFFECT,
                                trebleDb = (it.trebleDb - 1.5f).coerceIn(-12f, 12f)
                            )
                        }
                    },
                    onIncrease = {
                        onUpdateVocalSettings {
                            it.copy(
                                preset = VocalPreset.CUSTOM_EFFECT,
                                trebleDb = (it.trebleDb + 1.5f).coerceIn(-12f, 12f)
                            )
                        }
                    }
                )

                AudioStepperRow(
                    label = "Pitch Control (Nada Dasar)",
                    valueText = "${if (vocalSettings.pitchSemitones > 0) "+" else ""}${vocalSettings.pitchSemitones} Semitone",
                    onDecrease = {
                        onUpdateVocalSettings {
                            it.copy(
                                preset = VocalPreset.CUSTOM_EFFECT,
                                pitchSemitones = (it.pitchSemitones - 1).coerceIn(-6, 6)
                            )
                        }
                    },
                    onIncrease = {
                        onUpdateVocalSettings {
                            it.copy(
                                preset = VocalPreset.CUSTOM_EFFECT,
                                pitchSemitones = (it.pitchSemitones + 1).coerceIn(-6, 6)
                            )
                        }
                    }
                )
            }
        }

        // Voice Search & YouTube API Status
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(TvSurface)
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Bahasa Pencarian Suara & Status YouTube API",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                    Text(
                        text = if (isLiveYouTubeApi) {
                            "YouTube Data API v3: Terhubung • Voice Search terisolasi dari USB Mic"
                        } else {
                            "YouTube Data API v3: Mode Katalog Offline (Tambahkan YOUTUBE_API_KEY di Secrets panel untuk pencarian YouTube live)"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    TvFocusableSurface(
                        onClick = { onSetVoiceLanguage("id-ID") },
                        shape = RoundedCornerShape(14.dp),
                        containerColor = if (voiceLanguage == "id-ID") KaraokeCrimson else TvSurfaceElevated
                    ) {
                        Text(
                            text = "Bahasa Indonesia",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
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
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AudioStepperRow(
    label: String,
    valueText: String,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(TvSurfaceElevated)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TvFocusableSurface(
                onClick = onDecrease,
                shape = CircleShape,
                containerColor = TvSurface,
                focusedContainerColor = KaraokeCrimson,
                modifier = Modifier.size(38.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Remove,
                        contentDescription = "Kurangi $label",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Text(
                text = valueText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = KaraokeCyan,
                modifier = Modifier.width(105.dp)
            )
            TvFocusableSurface(
                onClick = onIncrease,
                shape = CircleShape,
                containerColor = TvSurface,
                focusedContainerColor = KaraokeCrimson,
                modifier = Modifier.size(38.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Tambah $label",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
