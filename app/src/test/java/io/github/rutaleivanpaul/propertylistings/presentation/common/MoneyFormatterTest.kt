package io.github.rutaleivanpaul.propertylistings.presentation.common

import io.github.rutaleivanpaul.propertylistings.domain.model.Currency
import io.github.rutaleivanpaul.propertylistings.domain.model.Money
import org.junit.Assert.assertEquals
import org.junit.Test

/** Unit tests for [MoneyFormatter]: symbol per currency, two decimals and grouping. */
class MoneyFormatterTest {

    @Test
    fun `formats each supported currency with its symbol and two decimals`() {
        assertEquals("€14.18", MoneyFormatter.format(Money(14.18, Currency.EUR)))
        assertEquals("$15.44", MoneyFormatter.format(Money(15.44, Currency.USD)))
        assertEquals("£12.11", MoneyFormatter.format(Money(12.11, Currency.GBP)))
    }

    @Test
    fun `groups thousands and pads decimals`() {
        assertEquals("€1,299.00", MoneyFormatter.format(Money(1299.0, Currency.EUR)))
        assertEquals("€0.50", MoneyFormatter.format(Money(0.5, Currency.EUR)))
    }
}
