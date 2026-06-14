package io.github.rutaleivanpaul.propertylistings.domain.model

/**
 * A property as the app uses it: a strict, validated, framework-agnostic model.
 *
 * Every field is non-null and already validated/clamped/defaulted by the mapper, so neither the
 * ViewModels nor the UI ever defend against bad API data — that work happens once, at the
 * boundary. City and country are denormalised from the response-level `location` onto each
 * property so the detail screen is self-contained after navigation.
 *
 * @property id stable identifier, used as the list key and the navigation argument.
 * @property name display name (non-blank; blank-named items are dropped by the mapper).
 * @property isFeatured whether the property carries the featured badge.
 * @property ratingOutOf10 overall rating on a 0.0–10.0 scale, one decimal. `0.0` means "no
 *   rating" (the API's 0 sentinel) — see [hasRating] — not a genuine zero score.
 * @property numberOfRatings how many ratings the score is based on (0 if unknown).
 * @property price the lowest price per night, in its base currency (EUR from the API).
 * @property overview short description, HTML-unescaped.
 * @property type the kind of accommodation.
 * @property city denormalised from the response-level location.
 * @property country denormalised from the response-level location.
 * @property imageUrls gallery image URLs; may be empty (images are optional/bonus).
 */
data class Property(
    val id: Int,
    val name: String,
    val isFeatured: Boolean,
    val ratingOutOf10: Double,
    val numberOfRatings: Int,
    val price: Money,
    val overview: String,
    val type: PropertyType,
    val city: String,
    val country: String,
    val imageUrls: List<String>,
) {
    /**
     * Whether this property has a meaningful rating to display.
     *
     * The API uses `0` to mean "not yet rated" rather than a literal zero score, so the UI shows
     * "No rating" instead of "0.0" in that case. Centralising the check here keeps that policy out
     * of the composables.
     */
    val hasRating: Boolean get() = ratingOutOf10 > 0.0
}
