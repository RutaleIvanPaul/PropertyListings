package io.github.rutaleivanpaul.propertylistings.presentation.common

import androidx.annotation.StringRes
import io.github.rutaleivanpaul.propertylistings.R
import io.github.rutaleivanpaul.propertylistings.domain.model.PropertyType

/**
 * The display string-resource for a [PropertyType] (e.g. HOSTEL → "Hostel").
 *
 * Lives in the presentation layer so the domain enum stays free of Android resource ids. Shared by
 * the list card and (in M4) the detail screen, so the label wording is defined once.
 */
@StringRes
fun PropertyType.labelRes(): Int = when (this) {
    PropertyType.HOSTEL -> R.string.property_type_hostel
    PropertyType.GUESTHOUSE -> R.string.property_type_guesthouse
    PropertyType.HOTEL -> R.string.property_type_hotel
    PropertyType.OTHER -> R.string.property_type_other
}
