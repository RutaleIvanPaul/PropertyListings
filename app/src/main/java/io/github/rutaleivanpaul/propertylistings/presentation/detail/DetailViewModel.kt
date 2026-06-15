package io.github.rutaleivanpaul.propertylistings.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.rutaleivanpaul.propertylistings.domain.converter.CurrencyConverter
import io.github.rutaleivanpaul.propertylistings.domain.model.Currency
import io.github.rutaleivanpaul.propertylistings.domain.model.Money
import io.github.rutaleivanpaul.propertylistings.domain.model.Property
import io.github.rutaleivanpaul.propertylistings.domain.model.Rates
import io.github.rutaleivanpaul.propertylistings.domain.repository.PropertyRepository
import io.github.rutaleivanpaul.propertylistings.domain.repository.RatesRepository
import io.github.rutaleivanpaul.propertylistings.presentation.navigation.DetailDestination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * MVI ViewModel for the property detail screen.
 *
 * Reuses the property already fetched for the list via [PropertyRepository.cachedProperty] — the
 * only network call this screen makes is for exchange rates ([RatesRepository.getRates]) to drive
 * the currency toggle. A missing property (cache evicted, or navigated to with an unknown id) is an
 * [DetailUiState.Error]; missing rates degrade the toggle to EUR-only rather than failing.
 *
 * All conversion decisions live here (which currencies are offered, what the displayed price is);
 * the composables are a pure function of [state] and only send [DetailIntent]s back.
 */
@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val propertyRepository: PropertyRepository,
    private val ratesRepository: RatesRepository,
) : ViewModel() {

    private val propertyId: Int = checkNotNull(savedStateHandle[DetailDestination.ARG_PROPERTY_ID]) {
        "Detail screen requires a '${DetailDestination.ARG_PROPERTY_ID}' navigation argument."
    }

    private val _state = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val state: StateFlow<DetailUiState> = _state.asStateFlow()

    /** Rates retained after the load so currency switches need no further fetch (or null = EUR-only). */
    private var rates: Rates? = null

    init {
        load()
    }

    /** Single entry point for the UI to drive the screen. */
    fun onIntent(intent: DetailIntent) {
        when (intent) {
            DetailIntent.Load, DetailIntent.Retry -> load()
            is DetailIntent.SelectCurrency -> selectCurrency(intent.currency)
        }
    }

    private fun load() {
        viewModelScope.launch {
            _state.value = DetailUiState.Loading

            val property = propertyRepository.cachedProperty(propertyId)
            if (property == null) {
                // Not in cache (e.g. process death before any list load, or an unknown id) → error.
                _state.value = DetailUiState.Error
                return@launch
            }

            // null rates → toggle degrades to EUR-only; the property's price is already in EUR.
            rates = ratesRepository.getRates()

            val available = availableCurrencies(property.price)
            val selected = Currency.EUR
            _state.value = DetailUiState.Content(
                property = property,
                selectedCurrency = selected,
                displayedPrice = priceIn(property.price, selected),
                availableCurrencies = available,
            )
        }
    }

    private fun selectCurrency(currency: Currency) {
        val content = _state.value as? DetailUiState.Content ?: return
        // Ignore a selection that isn't on offer (e.g. its rate was missing) — keeps state honest.
        if (currency !in content.availableCurrencies || currency == content.selectedCurrency) return

        _state.value = content.copy(
            selectedCurrency = currency,
            displayedPrice = priceIn(content.property.price, currency),
        )
    }

    /** The currencies the toggle can offer: EUR always, plus any whose rate is available. */
    private fun availableCurrencies(price: Money): List<Currency> =
        Currency.entries.filter { convert(price, it) != null }

    /** Converts [price] to [target], falling back to the unconverted price if a rate is missing. */
    private fun priceIn(price: Money, target: Currency): Money = convert(price, target) ?: price

    /**
     * Converts [price] to [target], or returns `null` when it cannot: a non-EUR target needs live
     * rates (absent → degrade) and a present rate for that currency.
     */
    private fun convert(price: Money, target: Currency): Money? {
        if (price.currency == target) return price
        val rates = rates ?: return null
        return CurrencyConverter.convert(price, rates, target)
    }
}
