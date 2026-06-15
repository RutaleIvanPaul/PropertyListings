package io.github.rutaleivanpaul.propertylistings.domain.model

/**
 * A single sub-score of a property's rating (e.g. cleanliness), on the same 0.0–10.0 scale as the
 * overall rating so the detail screen can present them consistently.
 *
 * The API publishes these as 0–100 integers; the mapper converts and clamps them via the shared
 * rating converter, so the UI only ever sees a validated [scoreOutOf10].
 *
 * @property category which aspect this score rates.
 * @property scoreOutOf10 the score on a 0.0–10.0 scale, one decimal.
 */
data class RatingScore(
    val category: RatingCategory,
    val scoreOutOf10: Double,
)

/**
 * The aspects a property can be rated on in the breakdown.
 *
 * A closed set mirroring the categories the API returns, so the detail screen maps each to a
 * localised label without leaking raw API keys. Declaration order is the display order.
 */
enum class RatingCategory {
    SECURITY,
    LOCATION,
    STAFF,
    FUN,
    CLEANLINESS,
    FACILITIES,
    VALUE,
}
