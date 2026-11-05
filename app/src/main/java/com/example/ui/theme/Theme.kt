package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val KoroKoroDarkColorScheme = darkColorScheme(
    primary = KaraokeCrimson,
    onPrimary = TextPrimary,
    primaryContainer = KaraokeCrimsonLight,
    onPrimaryContainer = TvBackground,
    secondary = KaraokeCyan,
    onSecondary = TvBackground,
    secondaryContainer = TvSurfaceElevated,
    onSecondaryContainer = KaraokeCyan,
    tertiary = KaraokeGold,
    onTertiary = TvBackground,
    background = TvBackground,
    onBackground = TextPrimary,
    surface = TvSurface,
    onSurface = TextPrimary,
    surfaceVariant = TvSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = DividerSubtle,
    outlineVariant = TextMuted
)

@Composable
fun KoroKoroTvTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = KoroKoroDarkColorScheme,
        typography = Typography,
        content = content
    )
}
