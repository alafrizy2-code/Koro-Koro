package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.DividerSubtle
import com.example.ui.theme.FocusBorderPrimary
import com.example.ui.theme.TvCardBg
import com.example.ui.theme.TvSurfaceElevated

@Composable
fun TvFocusableSurface(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String? = null,
    shape: Shape = RoundedCornerShape(14.dp),
    containerColor: Color = TvCardBg,
    focusedContainerColor: Color = TvSurfaceElevated,
    focusedBorderColor: Color = FocusBorderPrimary,
    unfocusedBorderColor: Color = DividerSubtle.copy(alpha = 0.45f),
    focusScale: Float = 1.035f,
    tonalElevation: Dp = 0.dp,
    onFocusChanged: ((Boolean) -> Unit)? = null,
    content: @Composable BoxScope.(isFocused: Boolean) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val animSpec = remember { tween<Float>(durationMillis = 120, easing = FastOutSlowInEasing) }
    val colorSpec = remember { tween<Color>(durationMillis = 120, easing = FastOutSlowInEasing) }

    val scale by animateFloatAsState(
        targetValue = if (isFocused) focusScale else 1f,
        animationSpec = animSpec,
        label = "tv_focus_scale"
    )
    val bgColor by animateColorAsState(
        targetValue = if (isFocused) focusedContainerColor else containerColor,
        animationSpec = colorSpec,
        label = "tv_focus_bg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isFocused) focusedBorderColor else unfocusedBorderColor,
        animationSpec = colorSpec,
        label = "tv_focus_border"
    )

    val taggedModifier = if (testTag != null) modifier.testTag(testTag) else modifier

    Surface(
        modifier = taggedModifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyUp &&
                    (event.key == Key.DirectionCenter || event.key == Key.Enter || event.key == Key.NumPadEnter)
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
            ),
        shape = shape,
        color = bgColor,
        tonalElevation = tonalElevation,
        shadowElevation = 0.dp,
        border = BorderStroke(
            width = if (isFocused) 2.5.dp else 1.dp,
            color = borderColor
        )
    ) {
        onFocusChanged?.invoke(isFocused)
        Box {
            content(isFocused)
        }
    }
}
