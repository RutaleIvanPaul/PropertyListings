package io.github.rutaleivanpaul.propertylistings.presentation.list

import org.junit.Assert.assertEquals
import org.junit.Test

/** Unit tests for [RatingTier.forRating] banding, including the boundaries and the 0 sentinel. */
class RatingTierTest {

    @Test
    fun `zero is the no-rating sentinel`() {
        assertEquals(RatingTier.NONE, RatingTier.forRating(0.0))
    }

    @Test
    fun `bands map at their lower boundaries`() {
        assertEquals(RatingTier.SUPERB, RatingTier.forRating(9.0))
        assertEquals(RatingTier.FABULOUS, RatingTier.forRating(8.0))
        assertEquals(RatingTier.VERY_GOOD, RatingTier.forRating(7.0))
        assertEquals(RatingTier.GOOD, RatingTier.forRating(6.0))
    }

    @Test
    fun `just below a boundary falls to the lower band`() {
        assertEquals(RatingTier.FABULOUS, RatingTier.forRating(8.9))
        assertEquals(RatingTier.FAIR, RatingTier.forRating(5.9))
    }

    @Test
    fun `a small positive rating is fair, not no-rating`() {
        assertEquals(RatingTier.FAIR, RatingTier.forRating(0.5))
    }

    @Test
    fun `the top of the scale is superb`() {
        assertEquals(RatingTier.SUPERB, RatingTier.forRating(10.0))
    }
}
