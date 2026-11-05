package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.QueueMusic
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.SidebarDestination
import com.example.ui.theme.DividerSubtle
import com.example.ui.theme.KaraokeCrimson
import com.example.ui.theme.KaraokeCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TvSidebarBg
import com.example.ui.theme.TvSurfaceElevated

@Composable
fun SidebarNav(
    currentDestination: SidebarDestination,
    isExpanded: Boolean,
    isUsbMicConnected: Boolean,
    onSidebarFocusChanged: (Boolean) -> Unit,
    onSelectDestination: (SidebarDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val sidebarWidth by animateDpAsState(
        targetValue = if (isExpanded) 236.dp else 78.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "sidebar_width"
    )

    val homeFocusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        try {
            homeFocusRequester.requestFocus()
        } catch (_: Exception) {
        }
    }

    Column(
        modifier = modifier
            .width(sidebarWidth)
            .fillMaxHeight()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        TvSidebarBg,
                        Color(0xFF0A0A0E)
                    )
                )
            )
            .border(width = 1.dp, color = DividerSubtle.copy(alpha = 0.4f))
            .onFocusChanged { state ->
                onSidebarFocusChanged(state.hasFocus)
            }
            .padding(vertical = 20.dp, horizontal = 12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Brand Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(KaraokeCrimson, Color(0xFF9C27B0))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Mic,
                        contentDescription = "Koro Koro TV",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        Text(
                            text = "Koro Koro",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 1
                        )
                        Text(
                            text = "KARAOKE TV",
                            style = MaterialTheme.typography.labelSmall,
                            color = KaraokeCyan,
                            letterSpacing = androidx.compose.ui.unit.TextUnit(1.5f, androidx.compose.ui.unit.TextUnitType.Sp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            SidebarDestination.entries.forEach { dest ->
                val isSelected = currentDestination == dest
                val itemModifier = if (dest == SidebarDestination.HOME) {
                    Modifier.focusRequester(homeFocusRequester)
                } else {
                    Modifier
                }
                SidebarNavItem(
                    destination = dest,
                    isSelected = isSelected,
                    isExpanded = isExpanded,
                    onClick = { onSelectDestination(dest) },
                    modifier = itemModifier
                )
            }
        }

        // Bottom USB Mic Status Pill
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(TvSurfaceElevated.copy(alpha = 0.7f))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (isExpanded) Arrangement.Start else Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (isUsbMicConnected) Color(0xFF00E676) else TextMuted)
            )
            if (isExpanded) {
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (isUsbMicConnected) "USB Mic Aktif" else "Mic Siap",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isUsbMicConnected) Color(0xFF00E676) else TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun SidebarNavItem(
    destination: SidebarDestination,
    isSelected: Boolean,
    isExpanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val bgColor by animateColorAsState(
        targetValue = when {
            isFocused -> KaraokeCrimson
            isSelected -> TvSurfaceElevated
            else -> Color.Transparent
        },
        label = "nav_item_bg"
    )
    val contentColor by animateColorAsState(
        targetValue = when {
            isFocused -> Color.White
            isSelected -> KaraokeCrimson
            else -> TextSecondary
        },
        label = "nav_item_fg"
    )

    val icon = destinationIcon(destination, isSelected || isFocused)

    Row(
        modifier = modifier
            .testTag("sidebar_${destination.route}")
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .then(
                if (isFocused) {
                    Modifier.border(1.5.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                } else {
                    Modifier
                }
            )
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyUp &&
                    (event.key == Key.DirectionCenter || event.key == Key.Enter)
                ) {
                    onClick()
                    true
                } else {
                    false
                }
            }
            .focusable(interactionSource = interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (isExpanded) Arrangement.Start else Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = destination.label,
            tint = contentColor,
            modifier = Modifier.size(22.dp)
        )
        if (isExpanded) {
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = destination.label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isSelected || isFocused) FontWeight.Bold else FontWeight.Medium,
                color = if (isFocused) Color.White else if (isSelected) TextPrimary else TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun destinationIcon(dest: SidebarDestination, active: Boolean): ImageVector {
    return when (dest) {
        SidebarDestination.HOME -> if (active) Icons.Filled.Home else Icons.Outlined.Home
        SidebarDestination.SEARCH -> if (active) Icons.Filled.Search else Icons.Outlined.Search
        SidebarDestination.VOICE_SEARCH -> if (active) Icons.Filled.Mic else Icons.Outlined.Mic
        SidebarDestination.CATEGORIES -> if (active) Icons.Filled.Category else Icons.Outlined.Category
        SidebarDestination.FAVORITES -> if (active) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder
        SidebarDestination.HISTORY -> if (active) Icons.Filled.History else Icons.Outlined.History
        SidebarDestination.PLAYLISTS -> if (active) Icons.Filled.QueueMusic else Icons.Outlined.QueueMusic
        SidebarDestination.SETTINGS -> if (active) Icons.Filled.Settings else Icons.Outlined.Settings
    }
}
