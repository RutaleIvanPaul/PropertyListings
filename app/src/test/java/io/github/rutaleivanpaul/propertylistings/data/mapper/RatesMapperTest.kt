package io.github.rutaleivanpaul.propertylistings.data.mapper

import io.github.rutaleivanpaul.propertylistings.data.remote.dto.RatesResponseDto
import io.github.rutaleivanpaul.propertylistings.domain.model.Currency
import io.github.rutaleivanpaul.propertylistings.util.loadFixture
import io.github.rutaleivanpaul.propertylistings.util.testJson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Unit tests for [RatesMapper]: base resolution and dropping of unsupported currency codes. */
class RatesMapperTest {

    @Test
    fun `maps base and retains only supported currencies`() {
        val rates = RatesMapper.map(
            testJson.decodeFromString<RatesResponseDto>(loadFixture("rates_sample.json")),
        )

        assertEquals(Currency.EUR, rates.base)
        assertEquals(1.0, rates.rates[Currency.EUR]!!, 0.0)
        assertEquals(1.088993, rates.rates[Currency.USD]!!, 0.0)
        assertEquals(0.853825, rates.rates[Currency.GBP]!!, 0.0)
        // JPY is present in the payload but unsupported, so it is discarded.
        assertEquals(3, rates.rates.size)
    }

    @Test
    fun `unknown or absent base defaults to EUR`() {
        assertEquals(Currency.EUR, RatesMapper.map(RatesResponseDto(base = "XYZ")).base)
        assertEquals(Currency.EUR, RatesMapper.map(RatesResponseDto(base = null)).base)
    }

    @Test
    fun `null rates map yields no usable target rates`() {
        val rates = RatesMapper.map(RatesResponseDto(base = "EUR", rates = null))
        assertTrue(rates.rates.isEmpty())
        assertFalse(rates.rates.containsKey(Currency.USD))
    }
}
