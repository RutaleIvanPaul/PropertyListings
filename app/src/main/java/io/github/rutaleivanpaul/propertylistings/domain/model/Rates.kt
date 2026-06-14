package io.github.rutaleivanpaul.propertylistings.domain.model

/**
 * Exchange rates expressed as units of each currency per one unit of [base].
 *
 * The API publishes a fixer-style payload based on EUR (e.g. `USD = 1.088993` means 1 EUR buys
 * 1.088993 USD). Only the supported [Currency] entries are retained; unknown codes are dropped at
 * the mapping boundary so the core never reasons about currencies it cannot display.
 *
 * @property base the currency all [rates] are relative to.
 * @property rates units of each currency per one unit of [base]. The base itself maps to `1.0`.
 */
data class Rates(
    val base: Currency,
    val rates: Map<Currency, Double>,
)
