package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.KaraokeCategory
import com.example.model.KaraokeSong
import com.example.ui.components.KaraokeVideoCard
import com.example.ui.components.TvFocusableSurface
import com.example.ui.theme.KaraokeCrimson
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun CategoriesScreen(
    categories: List<KaraokeCategory>,
    selectedCategory: KaraokeCategory?,
    categorySongs: List<KaraokeSong>,
    isLoading: Boolean,
    favoriteIds: Set<String>,
    onSelectCategory: (KaraokeCategory) -> Unit,
    onPlaySong: (KaraokeSong, List<KaraokeSong>) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 12.dp)
    ) {
        Text(
            text = "Kategori Karaoke",
            style = MaterialTheme.typography.headlineLarge,
            color = TextPrimary,
            modifier = Modifier.padding(horizontal = 28.dp)
        )
        Text(
            text = "Pilih genre atau suasana lagu yang ingin kamu nyanyikan",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.padding(horizontal = 28.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 28.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(categories, key = { it.id }) { cat ->
                val isSelected = selectedCategory?.id == cat.id
                val accent = Color(cat.accentColorHex)
                TvFocusableSurface(
                    onClick = { onSelectCategory(cat) },
                    testTag = "category_card_${cat.id}",
                    shape = RoundedCornerShape(16.dp),
                    focusedBorderColor = accent,
                    modifier = Modifier.width(210.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        accent.copy(alpha = if (isSelected) 0.42f else 0.18f),
                                        Color(0xFF161620)
                                    )
                                )
                            )
                            .padding(18.dp)
                    ) {
                        Column {
                            Text(
                                text = cat.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = cat.subtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = KaraokeCrimson)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 260.dp),
                contentPadding = PaddingValues(start = 28.dp, end = 28.dp, bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                items(categorySongs, key = { it.videoId }) { song ->
                    KaraokeVideoCard(
                        song = song,
                        isFavorite = favoriteIds.contains(song.videoId),
                        cardWidth = 280.dp,
                        onClick = { onPlaySong(song, categorySongs) }
                    )
                }
            }
        }
    }
}
