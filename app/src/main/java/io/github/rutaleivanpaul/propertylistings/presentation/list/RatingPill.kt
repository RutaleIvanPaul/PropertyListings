package io.github.rutaleivanpaul.propertylistings.presentation.list

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.rutaleivanpaul.propertylistings.R
import io.github.rutaleivanpaul.propertylistings.presentation.theme.RatingFabulousContainer
import io.github.rutaleivanpaul.propertylistings.presentation.theme.RatingFairContainer
import io.github.rutaleivanpaul.propertylistings.presentation.theme.RatingGoodContainer
import io.github.rutaleivanpaul.propertylistings.presentation.theme.RatingNoneContainer
import io.github.rutaleivanpaul.propertylistings.presentation.theme.RatingOnDarkContainer
import io.github.rutaleivanpaul.propertylistings.presentation.theme.RatingOnLightContainer
import io.github.rutaleivanpaul.propertylistings.presentation.theme.RatingSuperbContainer
import io.github.rutaleivanpaul.propertylistings.presentation.theme.RatingVeryGoodContainer
import java.util.Locale

/**
 * The quality-graded rating pill (Option A): a compact, colour-coded `/10` score.
 *
 * The tier banding ([RatingTier.forRating]) drives both the colour and the spoken label. The
 * pill collapses its inner text into a single descriptive semantics node so screen readers announce
 * "Rated 8.7 out of 10, Fabulous" rather than a bare number; a `0.0` rating shows "No rating".
 *
 * @param rating the score on a 0.0–10.0 scale (`0.0` means "no rating").
 */
@Composable
fun RatingPill(
    rating: Double,
    modifier: Modifier = Modifier,
) {
    val tier = RatingTier.forRating(rating)
    val (container, content) = tier.colors()
    val tierLabel = stringResource(tier.labelRes)

    val text = if (tier == RatingTier.NONE) {
        stringResource(R.string.rating_none)
    } else {
        String.format(Locale.US, "%.1f", rating)
    }
    val description = if (tier == RatingTier.NONE) {
        stringResource(R.string.rating_description_none)
    } else {
        stringResource(R.string.rating_description, text, tierLabel)
    }

    Surface(
        color = container,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.clearAndSetSemantics { contentDescription = description },
    ) {
        Text(
            text = text,
            color = content,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

/** The container/content colour pair for this tier; amber uses dark content for legibility. */
private fun RatingTier.colors(): Pair<Color, Color> = when (this) {
    RatingTier.SUPERB -> RatingSuperbContainer to RatingOnDarkContainer
    RatingTier.FABULOUS -> RatingFabulousContainer to RatingOnDarkContainer
    RatingTier.VERY_GOOD -> RatingVeryGoodContainer to RatingOnDarkContainer
    RatingTier.GOOD -> RatingGoodContainer to RatingOnLightContainer
    RatingTier.FAIR -> RatingFairContainer to RatingOnDarkContainer
    RatingTier.NONE -> RatingNoneContainer to RatingOnDarkContainer
}
