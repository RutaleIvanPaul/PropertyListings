package io.github.rutaleivanpaul.propertylistings.data.mapper

import io.github.rutaleivanpaul.propertylistings.data.remote.dto.ImageDto
import io.github.rutaleivanpaul.propertylistings.data.remote.dto.PropertiesResponseDto
import io.github.rutaleivanpaul.propertylistings.data.remote.dto.PropertyDto
import io.github.rutaleivanpaul.propertylistings.domain.converter.RatingConverter
import io.github.rutaleivanpaul.propertylistings.domain.model.Currency
import io.github.rutaleivanpaul.propertylistings.domain.model.Money
import io.github.rutaleivanpaul.propertylistings.domain.model.Property
import io.github.rutaleivanpaul.propertylistings.domain.model.PropertyType

/**
 * Maps the tolerant [PropertiesResponseDto] to strict domain [Property] models.
 *
 * This is where the brief's per-item failure modes are absorbed so the rest of the app sees only
 * clean data:
 * - **Missing essential fields (mode c):** an item without an `id`, a usable `name`, or a parseable
 *   price is dropped — the rest of the list still renders.
 * - **Corrupt values (mode d):** out-of-range ratings are clamped (in [RatingConverter]) and a
 *   negative price is clamped to zero, rather than rejected.
 *
 * City and country come from the response-level [PropertiesResponseDto.location] and are
 * denormalised onto every property so the detail screen needs no shared state after navigation.
 */
object PropertyMapper {

    private const val IMAGE_BASE_URL = "https://"

    /** Maps the whole response, dropping any item that fails the essential-field checks. */
    fun map(dto: PropertiesResponseDto): List<Property> {
        val city = dto.location?.city?.name.orEmpty()
        val country = dto.location?.city?.country.orEmpty()
        return dto.properties.orEmpty().mapNotNull { it?.toDomain(city, country) }
    }

    /** @return the domain model, or `null` if an essential field is missing/unusable. */
    private fun PropertyDto.toDomain(city: String, country: String): Property? {
        val safeId = id ?: return null
        val safeName = name?.trim()?.takeIf { it.isNotBlank() } ?: return null
        val amount = lowestPricePerNight?.value?.toDoubleOrNull() ?: return null

        val currency = Currency.fromCode(lowestPricePerNight?.currency) ?: Currency.EUR

        return Property(
            id = safeId,
            name = safeName,
            isFeatured = isFeatured ?: false,
            ratingOutOf10 = RatingConverter.toRatingOutOf10(overallRating?.overall),
            numberOfRatings = overallRating?.numberOfRatings?.toIntOrNull()?.coerceAtLeast(0) ?: 0,
            // A negative price is corrupt (mode d) → clamp to zero rather than drop the item.
            price = Money(amount = amount.coerceAtLeast(0.0), currency = currency),
            overview = HtmlUnescape.unescape(overview.orEmpty()),
            type = PropertyType.fromApi(type),
            city = city,
            country = country,
            imageUrls = imagesGallery.orEmpty().mapNotNull { it?.toUrl() },
        )
    }

    /** Builds a full image URL from the host+path [ImageDto.prefix] and filename [ImageDto.suffix]. */
    private fun ImageDto.toUrl(): String? {
        val safePrefix = prefix?.takeIf { it.isNotBlank() } ?: return null
        val safeSuffix = suffix?.takeIf { it.isNotBlank() } ?: return null
        return IMAGE_BASE_URL + safePrefix + safeSuffix
    }
}
