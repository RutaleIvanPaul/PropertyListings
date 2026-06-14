package io.github.rutaleivanpaul.propertylistings.domain.model

/**
 * A monetary amount in a specific [Currency].
 *
 * Pairing the amount with its currency (rather than passing bare [Double]s) keeps conversions
 * honest: the [io.github.rutaleivanpaul.propertylistings.domain.converter.CurrencyConverter] can
 * verify the source currency against the rates' base instead of assuming it.
 *
 * @property amount the value, guaranteed non-negative by the mapper that constructs it.
 * @property currency the currency the [amount] is expressed in.
 */
data class Money(
    val amount: Double,
    val currency: Currency,
)
