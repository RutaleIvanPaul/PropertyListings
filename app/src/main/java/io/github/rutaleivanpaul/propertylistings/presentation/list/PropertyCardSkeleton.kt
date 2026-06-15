package io.github.rutaleivanpaul.propertylistings.presentation.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.rutaleivanpaul.propertylistings.presentation.common.shimmer

/** Number of placeholder cards shown while the list loads — enough to fill a typical screen. */
private const val SKELETON_CARD_COUNT = 6

/**
 * Loading placeholder that mirrors [PropertyCard]'s layout — the square thumbnail box plus stacked
 * text lines and the bottom pill/price row — so the loading state previews the shape of the content
 * that will replace it, rather than a bare spinner.
 *
 * Every block is a shimmering neutral rectangle; the card chrome (shape, elevation, padding, the
 * 92dp thumbnail, the 12dp gap) matches the real card exactly so there is no visual jump when real
 * data arrives. Purely decorative — no text, no semantics.
 */
@Composable
fun PropertyCardSkeleton(modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(ThumbnailSize)
                    .clip(RoundedCornerShape(12.dp))
                    .shimmer(),
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                SkeletonLine(widthFraction = 0.7f, height = 18.dp) // name
                Spacer(Modifier.height(8.dp))
                SkeletonLine(widthFraction = 0.5f, height = 14.dp) // type · location
                Spacer(Modifier.height(8.dp))
                SkeletonLine(widthFraction = 0.4f, height = 12.dp) // rating caption
                Spacer(Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    SkeletonBlock(width = 56.dp, height = 24.dp) // rating pill
                    SkeletonBlock(width = 72.dp, height = 20.dp) // price
                }
            }
        }
    }
}

/** A full row of [PropertyCardSkeleton]s, laid out exactly like the real list (same padding/spacing). */
@Composable
fun PropertyListSkeleton(modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(PaddingValues(16.dp)),
    ) {
        repeat(SKELETON_CARD_COUNT) {
            PropertyCardSkeleton()
        }
    }
}

/** A shimmering text-line placeholder sized as a fraction of the available width. */
@Composable
private fun SkeletonLine(widthFraction: Float, height: Dp) {
    Box(
        modifier = Modifier
            .fillMaxWidth(widthFraction)
            .height(height)
            .clip(RoundedCornerShape(4.dp))
            .shimmer(),
    )
}

/** A shimmering fixed-size block placeholder (rating pill / price). */
@Composable
private fun SkeletonBlock(width: Dp, height: Dp) {
    Box(
        modifier = Modifier
            .width(width)
            .height(height)
            .clip(RoundedCornerShape(8.dp))
            .shimmer(),
    )
}
