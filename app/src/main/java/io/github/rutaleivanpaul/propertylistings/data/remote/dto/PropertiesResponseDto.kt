package io.github.rutaleivanpaul.propertylistings.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Tolerant DTOs for the `properties.json` payload.
 *
 * Every field is nullable on purpose: this is the untrusted boundary, so the parser must never
 * fail because a value is absent, null or the wrong-but-coercible shape. Validation, defaulting and
 * clamping happen afterwards in the mapper, which turns these into strict domain models. Unknown
 * keys (the payload carries ~40 fields the app ignores) are dropped by the `Json` configuration.
 */
@Serializable
data class PropertiesResponseDto(
    val location: LocationDto? = null,
    val properties: List<PropertyDto?>? = null,
)

/** Response-level location, shared by all properties (denormalised onto each domain property). */
@Serializable
data class LocationDto(
    val city: CityDto? = null,
)

@Serializable
data class CityDto(
    val name: String? = null,
    val country: String? = null,
)

@Serializable
data class PropertyDto(
    val id: Int? = null,
    val name: String? = null,
    val isFeatured: Boolean? = null,
    val overallRating: OverallRatingDto? = null,
    val lowestPricePerNight: PriceDto? = null,
    val overview: String? = null,
    val type: String? = null,
    val address1: String? = null,
    val address2: String? = null,
    val imagesGallery: List<ImageDto?>? = null,
)

@Serializable
data class OverallRatingDto(
    val overall: Int? = null,
    /** A digit string (e.g. "11133") in the payload; parsed tolerantly in the mapper. */
    val numberOfRatings: String? = null,
)

/** Lowest price per night: `value` is a numeric string and `currency` is "EUR" in all real data. */
@Serializable
data class PriceDto(
    val value: String? = null,
    val currency: String? = null,
)

/** A gallery image split into host+path [prefix] and filename [suffix]; combined in the mapper. */
@Serializable
data class ImageDto(
    val prefix: String? = null,
    val suffix: String? = null,
)
