package io.github.rutaleivanpaul.propertylistings.presentation.common

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize

/** One sweep of the highlight across an element. */
private const val SHIMMER_DURATION_MILLIS = 1_200

/**
 * Paints the receiver with a subtle, continuously animated diagonal gradient — the standard
 * "shimmer" used for skeleton placeholders.
 *
 * The sweep runs between the neutral [base] block colour and a lighter [highlight], so a placeholder
 * reads as a shaped grey block with a soft light moving across it. The animated offset is derived
 * from the element's own measured width (via [onSizeChanged]), so the modifier is self-contained and
 * works on any sized box without the caller supplying dimensions.
 *
 * Purely decorative: it draws a background only and adds no semantics, so it is invisible to screen
 * readers (the loading container carries the spoken "loading" description instead).
 */
@Composable
fun Modifier.shimmer(): Modifier {
    val base = MaterialTheme.colorScheme.surfaceVariant
    val highlight = MaterialTheme.colorScheme.surface

    var size by remember { mutableStateOf(IntSize.Zero) }
    val transition = rememberInfiniteTransition(label = "shimmer")
    val offsetX by transition.animateFloat(
        initialValue = -2f * size.width,
        targetValue = 2f * size.width,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = SHIMMER_DURATION_MILLIS, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmer-offset",
    )

    return onSizeChanged { size = it }.background(
        brush = Brush.linearGradient(
            colors = listOf(base, highlight, base),
            start = Offset(offsetX, 0f),
            end = Offset(offsetX + size.width, size.height.toFloat()),
        ),
    )
}
