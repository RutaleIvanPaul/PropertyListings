package io.github.rutaleivanpaul.propertylistings.presentation.detail

import io.github.rutaleivanpaul.propertylistings.domain.model.Currency

/**
 * User intents for the property detail screen (MVI).
 *
 * Handling is wired in the detail-screen milestone.
 */
sealed interface DetailIntent {

    /** Load the selected property and the exchange rates needed for the price toggle. */
    data object Load : DetailIntent

    /** Retry after an error. */
    data object Retry : DetailIntent

    /** Switch the displayed lowest price to the given [currency]. */
    data class SelectCurrency(val currency: Currency) : DetailIntent
}
