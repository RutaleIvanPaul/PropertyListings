package io.github.rutaleivanpaul.propertylistings.presentation.common

import androidx.annotation.StringRes
import io.github.rutaleivanpaul.propertylistings.R
import io.github.rutaleivanpaul.propertylistings.domain.model.RatingCategory

/**
 * The display string-resource for a [RatingCategory] (e.g. CLEANLINESS → "Cleanliness").
 *
 * Lives in the presentation layer so the domain enum stays free of Android resource ids, mirroring
 * [io.github.rutaleivanpaul.propertylistings.presentation.common.labelRes] for property types.
 */
@StringRes
fun RatingCategory.labelRes(): Int = when (this) {
    RatingCategory.SECURITY -> R.string.rating_category_security
    RatingCategory.LOCATION -> R.string.rating_category_location
    RatingCategory.STAFF -> R.string.rating_category_staff
    RatingCategory.FUN -> R.string.rating_category_fun
    RatingCategory.CLEANLINESS -> R.string.rating_category_cleanliness
    RatingCategory.FACILITIES -> R.string.rating_category_facilities
    RatingCategory.VALUE -> R.string.rating_category_value
}
