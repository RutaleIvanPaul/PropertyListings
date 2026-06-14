package io.github.rutaleivanpaul.propertylistings.presentation.list

import androidx.annotation.StringRes
import io.github.rutaleivanpaul.propertylistings.R

/**
 * Quality grade for a rating (Option A): the tier drives the rating pill's colour and label.
 *
 * Pure mapping with no Android UI dependencies (only a string-resource id), so the threshold logic
 * is unit-testable. The composable resolves the tier to concrete colours; this type owns only the
 * banding and the label text.
 *
 * @property labelRes the human label for the tier (e.g. "Superb").
 */
enum class RatingTier(@StringRes val labelRes: Int) {
    SUPERB(R.string.rating_tier_superb),
    FABULOUS(R.string.rating_tier_fabulous),
    VERY_GOOD(R.string.rating_tier_very_good),
    GOOD(R.string.rating_tier_good),
    FAIR(R.string.rating_tier_fair),

    /** No meaningful score (the API's 0 sentinel); rendered as "No rating" with a neutral colour. */
    NONE(R.string.rating_none);

    companion object {
        /**
         * Bands a 0.0–10.0 rating into a tier. A rating of `0.0` is the "no rating" sentinel and maps
         * to [NONE]; any positive value bands by the usual thresholds.
         */
        fun forRating(ratingOutOf10: Double): RatingTier = when {
            ratingOutOf10 <= 0.0 -> NONE
            ratingOutOf10 >= 9.0 -> SUPERB
            ratingOutOf10 >= 8.0 -> FABULOUS
            ratingOutOf10 >= 7.0 -> VERY_GOOD
            ratingOutOf10 >= 6.0 -> GOOD
            else -> FAIR
        }
    }
}
