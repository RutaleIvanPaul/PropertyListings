package io.github.rutaleivanpaul.propertylistings.domain.converter

import io.github.rutaleivanpaul.propertylistings.domain.model.Currency
import io.github.rutaleivanpaul.propertylistings.domain.model.Money
import io.github.rutaleivanpaul.propertylistings.domain.model.Rates

/**
 * Converts a [Money] amount into a target [Currency] using a set of [Rates].
 *
 * Pure and stateless. Although the API prices everything in EUR (the same base as the rates), the
 * general cross-rate form is implemented so a future non-EUR price would still convert correctly —
 * it costs nothing and removes a hidden assumption. Returns `null` when a required rate is absent,
 * letting the caller honour the missing-rate policy (omit/disable that currency) rather than
 * showing a wrong number.
 */
object CurrencyConverter {

    /**
     * @return [money] expressed in [target], or `null` if a rate needed for the conversion is
     *   missing. Converting to the same currency returns the amount unchanged.
     */
    fun convert(money: Money, rates: Rates, target: Currency): Money? {
        if (money.currency == target) return money

        val fromRate = rateOf(money.currency, rates) ?: return null
        val toRate = rateOf(target, rates) ?: return null

        // General cross-rate: normalise to base, then to target. Collapses to a single multiply
        // when the source already is the base (fromRate == 1.0), which is the common EUR case.
        val converted = money.amount / fromRate * toRate
        return Money(amount = converted, currency = target)
    }

    /** Units of [currency] per one unit of the rates' base; the base itself is `1.0`. */
    private fun rateOf(currency: Currency, rates: Rates): Double? =
        if (currency == rates.base) 1.0 else rates.rates[currency]
}
