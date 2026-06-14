package io.github.rutaleivanpaul.propertylistings.domain.converter

import io.github.rutaleivanpaul.propertylistings.domain.model.Currency
import io.github.rutaleivanpaul.propertylistings.domain.model.Money
import io.github.rutaleivanpaul.propertylistings.domain.model.Rates
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit tests for [CurrencyConverter], exercised against the real rates from the API snapshot
 * (EUR base; USD 1.088993; GBP 0.853825) plus the missing-rate and cross-rate edge cases.
 */
class CurrencyConverterTest {

    private val rates = Rates(
        base = Currency.EUR,
        rates = mapOf(
            Currency.EUR to 1.0,
            Currency.USD to 1.088993,
            Currency.GBP to 0.853825,
        ),
    )

    @Test
    fun `converts EUR to USD using the live rate`() {
        val result = CurrencyConverter.convert(Money(14.18, Currency.EUR), rates, Currency.USD)
        assertEquals(14.18 * 1.088993, result!!.amount, 1e-9)
        assertEquals(Currency.USD, result.currency)
    }

    @Test
    fun `converts EUR to GBP using the live rate`() {
        val result = CurrencyConverter.convert(Money(14.18, Currency.EUR), rates, Currency.GBP)
        assertEquals(14.18 * 0.853825, result!!.amount, 1e-9)
    }

    @Test
    fun `same-currency conversion returns the amount unchanged`() {
        val money = Money(14.18, Currency.EUR)
        assertEquals(money, CurrencyConverter.convert(money, rates, Currency.EUR))
    }

    @Test
    fun `missing target rate returns null`() {
        val withoutGbp = rates.copy(rates = rates.rates - Currency.GBP)
        assertNull(CurrencyConverter.convert(Money(14.18, Currency.EUR), withoutGbp, Currency.GBP))
    }

    @Test
    fun `cross-rate from a non-base source currency is correct`() {
        // 10 USD -> GBP via the general form: amount / fromRate * toRate.
        val result = CurrencyConverter.convert(Money(10.0, Currency.USD), rates, Currency.GBP)
        assertEquals(10.0 / 1.088993 * 0.853825, result!!.amount, 1e-9)
        assertEquals(Currency.GBP, result.currency)
    }

    @Test
    fun `base currency is treated as rate one even if absent from the map`() {
        val baseMissing = rates.copy(rates = rates.rates - Currency.EUR)
        val result = CurrencyConverter.convert(Money(14.18, Currency.EUR), baseMissing, Currency.USD)
        assertEquals(14.18 * 1.088993, result!!.amount, 1e-9)
    }
}
