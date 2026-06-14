package io.github.rutaleivanpaul.propertylistings.data.mapper

import io.github.rutaleivanpaul.propertylistings.data.remote.dto.PropertiesResponseDto
import io.github.rutaleivanpaul.propertylistings.domain.model.Currency
import io.github.rutaleivanpaul.propertylistings.domain.model.PropertyType
import io.github.rutaleivanpaul.propertylistings.util.loadFixture
import io.github.rutaleivanpaul.propertylistings.util.testJson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [PropertyMapper], driven from a captured JSON fixture so the real parse settings
 * (unknown-key tolerance) and the four per-item behaviours are exercised end to end.
 */
class PropertyMapperTest {

    private fun mapFixture() =
        PropertyMapper.map(testJson.decodeFromString<PropertiesResponseDto>(loadFixture("properties_sample.json")))

    @Test
    fun `maps a valid property fully and denormalises city and country`() {
        val abbey = mapFixture().first { it.id == 1 }

        assertEquals("Abbey Court Hostel", abbey.name)
        assertTrue(abbey.isFeatured)
        assertEquals(8.7, abbey.ratingOutOf10, 0.0)
        assertTrue(abbey.hasRating)
        assertEquals(11133, abbey.numberOfRatings)
        assertEquals(14.18, abbey.price.amount, 0.0)
        assertEquals(Currency.EUR, abbey.price.currency)
        assertEquals(PropertyType.HOSTEL, abbey.type)
        assertEquals("Dublin", abbey.city)
        assertEquals("Ireland", abbey.country)
        // HTML entities in the overview are decoded.
        assertEquals("Dublin's liveliest hostel & a great base.", abbey.overview)
        assertEquals(
            listOf("https://res.cloudinary.com/test/v1/propertyimages/1/100/36.jpg"),
            abbey.imageUrls,
        )
    }

    @Test
    fun `rating of zero is mapped as no rating`() {
        val egans = mapFixture().first { it.id == 5575 }
        assertEquals(0.0, egans.ratingOutOf10, 0.0)
        assertFalse(egans.hasRating)
    }

    @Test
    fun `negative price is clamped to zero rather than dropped`() {
        val inn = mapFixture().first { it.id == 42 }
        assertEquals(0.0, inn.price.amount, 0.0)
    }

    @Test
    fun `items missing an id or an unparseable price are dropped, the rest still map`() {
        val ids = mapFixture().map { it.id }
        // Kept: 1, 5575, 42. Dropped: null-id item and the "N/A"-price item.
        assertEquals(listOf(1, 5575, 42), ids)
    }

    @Test
    fun `an empty or property-less response maps to an empty list`() {
        assertTrue(PropertyMapper.map(PropertiesResponseDto()).isEmpty())
    }
}
