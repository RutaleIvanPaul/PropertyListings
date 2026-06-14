package io.github.rutaleivanpaul.propertylistings.domain.converter

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for [RatingConverter] — the 0–100 → 0.0–10.0 conversion, plus the defensive null,
 * clamp and out-of-range handling required by the brief's "corrupt values" failure mode.
 */
class RatingConverterTest {

    @Test
    fun `typical rating converts to one decimal`() {
        assertEquals(8.7, RatingConverter.toRatingOutOf10(87), 0.0)
    }

    @Test
    fun `boundary values convert exactly`() {
        assertEquals(0.0, RatingConverter.toRatingOutOf10(0), 0.0)
        assertEquals(10.0, RatingConverter.toRatingOutOf10(100), 0.0)
        assertEquals(9.5, RatingConverter.toRatingOutOf10(95), 0.0)
    }

    @Test
    fun `null rating defaults to zero`() {
        assertEquals(0.0, RatingConverter.toRatingOutOf10(null), 0.0)
    }

    @Test
    fun `above-range value is clamped to ten`() {
        assertEquals(10.0, RatingConverter.toRatingOutOf10(150), 0.0)
    }

    @Test
    fun `negative value is clamped to zero`() {
        assertEquals(0.0, RatingConverter.toRatingOutOf10(-10), 0.0)
    }
}
