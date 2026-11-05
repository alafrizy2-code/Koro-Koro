package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.FocusBorderSecondary
import com.example.ui.theme.KaraokeCrimson
import com.example.ui.theme.KaraokeCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TvSurface
import com.example.ui.theme.TvSurfaceElevated

@Composable
fun TopSearchBar(
    currentQuery: String,
    isLiveYouTubeApi: Boolean,
    onOpenTextSearch: () -> Unit,
    onOpenVoiceSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Text Search Trigger Bar
        TvFocusableSurface(
            onClick = onOpenTextSearch,
            testTag = "top_search_bar",
            shape = RoundedCornerShape(28.dp),
            containerColor = TvSurface,
            focusedContainerColor = TvSurfaceElevated,
            focusScale = 1.02f,
            modifier = Modifier
                .weight(1f)
                .height(52.dp)
        ) { isFocused ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Cari lagu karaoke",
                    tint = if (isFocused) KaraokeCrimson else TextSecondary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = if (currentQuery.isBlank()) {
                        "Cari judul lagu, nama penyanyi, atau kata kunci karaoke..."
                    } else {
                        currentQuery
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (currentQuery.isBlank()) TextSecondary else TextPrimary,
                    maxLines = 1
                )
            }
        }

        // Dedicated Voice Search Microphone Button (Wajib & Tidak Boleh Dihilangkan)
        TvFocusableSurface(
            onClick = onOpenVoiceSearch,
            testTag = "top_voice_search_button",
            shape = RoundedCornerShape(28.dp),
            containerColor = TvSurfaceElevated,
            focusedContainerColor = KaraokeCrimson,
            focusedBorderColor = FocusBorderSecondary,
            focusScale = 1.06f,
            modifier = Modifier.height(52.dp)
        ) { isFocused ->
            Row(
                modifier = Modifier
                    .height(52.dp)
                    .padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            if (isFocused) Color.White.copy(alpha = 0.2f)
                            else KaraokeCrimson.copy(alpha = 0.18f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Mic,
                        contentDescription = "Pencarian Suara",
                        tint = if (isFocused) Color.White else KaraokeCrimson,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Cari Suara",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isFocused) Color.White else TextPrimary
                )
            }
        }

        // Subtle API Mode Badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (isLiveYouTubeApi) KaraokeCyan.copy(alpha = 0.14f)
                    else TvSurfaceElevated
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = if (isLiveYouTubeApi) "YouTube Live" else "Katalog Siap",
                style = MaterialTheme.typography.labelSmall,
                color = if (isLiveYouTubeApi) KaraokeCyan else TextSecondary
            )
        }
    }
}
