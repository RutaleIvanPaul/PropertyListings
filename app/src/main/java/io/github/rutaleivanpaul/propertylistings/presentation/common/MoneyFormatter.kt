package io.github.rutaleivanpaul.propertylistings.presentation.common

import io.github.rutaleivanpaul.propertylistings.domain.model.Currency
import io.github.rutaleivanpaul.propertylistings.domain.model.Money
import java.util.Locale

/**
 * Formats a [Money] for display, e.g. `Money(14.18, EUR)` → "€14.18".
 *
 * Pure and locale-stable (uses [Locale.US] for grouping/decimals) so the output is deterministic
 * and unit-testable; the surrounding "/ night" phrasing is supplied by a string resource at the
 * call site, keeping user-facing wording in resources.
 */
object MoneyFormatter {

    private fun symbolOf(currency: Currency): String = when (currency) {
        Currency.EUR -> "€"
        Currency.USD -> "$"
        Currency.GBP -> "£"
    }

    /** @return the amount with its currency symbol and two decimals (e.g. "$1,299.00"). */
    fun format(money: Money): String =
        symbolOf(money.currency) + String.format(Locale.US, "%,.2f", money.amount)
}
