package io.github.rutaleivanpaul.propertylistings.presentation.detail

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

    /**
     * Switch the displayed lowest price to another currency.
     *
     * Carries the ISO currency code for now; this becomes a `Currency` domain enum once the
     * domain model is introduced in the data-layer milestone.
     */
    data class SelectCurrency(val currencyCode: String) : DetailIntent
}
