package io.github.rutaleivanpaul.propertylistings.domain.converter

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Converts the API's 0–100 overall rating to a one-decimal value on a 0–10 scale.
 *
 * Pure and stateless so it is trivially unit-testable. The defensive handling (clamp, null) exists
 * for robustness even though the current data is clean: the real values are all integers in range,
 * so the rounding mode is not actually exercised in production — it is documented and correct for a
 * hypothetical fractional or out-of-range input.
 */
object RatingConverter {

    private const val MIN_RAW = 0
    private const val MAX_RAW = 100
    private const val SCALE_DIVISOR = 10.0

    /**
     * @param raw the API `overallRating.overall` (0–100), possibly `null` or out of range.
     * @return the rating on a 0.0–10.0 scale, rounded HALF_UP to one decimal. A `null` or
     *   out-of-range input is clamped into `0..100` first (so `null` → `0.0`). A result of `0.0`
     *   carries the "no rating" meaning the UI interprets — this converter does not special-case it.
     */
    fun toRatingOutOf10(raw: Int?): Double {
        val clamped = (raw ?: MIN_RAW).coerceIn(MIN_RAW, MAX_RAW)
        return BigDecimal(clamped / SCALE_DIVISOR)
            .setScale(1, RoundingMode.HALF_UP)
            .toDouble()
    }
}
