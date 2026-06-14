package io.github.rutaleivanpaul.propertylistings.presentation.detail

import io.github.rutaleivanpaul.propertylistings.domain.model.Currency
import io.github.rutaleivanpaul.propertylistings.domain.model.Money
import io.github.rutaleivanpaul.propertylistings.domain.model.Property

/**
 * Immutable UI state for the property detail screen (MVI).
 *
 * There is deliberately no `Empty` case (unlike the list): a property either exists in the cache
 * that backs this screen or it does not, and a missing property is an error condition (with retry),
 * not a meaningful "no results" state. Folding "not found" into [Error] keeps the states honest.
 */
sealed interface DetailUiState {

    /** Loading the selected property's details and exchange rates. */
    data object Loading : DetailUiState

    /**
     * Details loaded successfully and available to render.
     *
     * @property property the selected property.
     * @property selectedCurrency the currency the price is currently shown in.
     * @property displayedPrice the lowest price converted to [selectedCurrency].
     * @property availableCurrencies the currencies the toggle can offer; collapses to just EUR when
     *   live rates are unavailable (graceful degrade).
     */
    data class Content(
        val property: Property,
        val selectedCurrency: Currency,
        val displayedPrice: Money,
        val availableCurrencies: List<Currency>,
    ) : DetailUiState

    /** Load failed, or the requested property was not found; the screen offers a retry. */
    data object Error : DetailUiState
}
