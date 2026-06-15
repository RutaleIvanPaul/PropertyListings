package io.github.rutaleivanpaul.propertylistings.presentation.detail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import io.github.rutaleivanpaul.propertylistings.domain.DataResult
import io.github.rutaleivanpaul.propertylistings.domain.model.Currency
import io.github.rutaleivanpaul.propertylistings.domain.model.Money
import io.github.rutaleivanpaul.propertylistings.domain.model.Property
import io.github.rutaleivanpaul.propertylistings.domain.model.PropertyType
import io.github.rutaleivanpaul.propertylistings.domain.model.Rates
import io.github.rutaleivanpaul.propertylistings.domain.repository.PropertyRepository
import io.github.rutaleivanpaul.propertylistings.domain.repository.RatesRepository
import io.github.rutaleivanpaul.propertylistings.presentation.navigation.DetailDestination
import io.github.rutaleivanpaul.propertylistings.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for [DetailViewModel]: cache reuse, the EUR-base currency conversion that drives the
 * toggle, the missing-rate degrade paths, and the not-found error path. Uses fakes for both
 * repositories so the ViewModel is exercised against its abstractions, and Turbine for the
 * StateFlow.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val rates = Rates(
        base = Currency.EUR,
        rates = mapOf(
            Currency.EUR to 1.0,
            Currency.USD to 1.088993,
            Currency.GBP to 0.853825,
        ),
    )

    private class FakePropertyRepository(private val property: Property?) : PropertyRepository {
        override suspend fun getProperties(forceRefresh: Boolean): DataResult<List<Property>> =
            DataResult.Success(listOfNotNull(property))

        override fun cachedProperty(id: Int): Property? = property?.takeIf { it.id == id }
    }

    private class FakeRatesRepository(private val rates: Rates?) : RatesRepository {
        override suspend fun getRates(): Rates? = rates
    }

    private fun property(id: Int = 1) = Property(
        id = id,
        name = "Abbey Court Hostel",
        isFeatured = true,
        ratingOutOf10 = 8.7,
        numberOfRatings = 11133,
        price = Money(14.18, Currency.EUR),
        overview = "A great base.",
        type = PropertyType.HOSTEL,
        city = "Dublin",
        country = "Ireland",
        imageUrls = emptyList(),
    )

    private fun viewModel(
        property: Property? = property(),
        rates: Rates? = this.rates,
        id: Int = 1,
    ) = DetailViewModel(
        savedStateHandle = SavedStateHandle(mapOf(DetailDestination.ARG_PROPERTY_ID to id)),
        propertyRepository = FakePropertyRepository(property),
        ratesRepository = FakeRatesRepository(rates),
    )

    @Test
    fun `loads cached property in EUR and offers all three currencies when rates are present`() =
        runTest(mainDispatcherRule.dispatcher) {
            val vm = viewModel()
            advanceUntilIdle()

            val content = vm.state.value as DetailUiState.Content
            assertEquals(1, content.property.id)
            assertEquals(Currency.EUR, content.selectedCurrency)
            assertEquals(Money(14.18, Currency.EUR), content.displayedPrice)
            assertEquals(listOf(Currency.EUR, Currency.USD, Currency.GBP), content.availableCurrencies)
        }

    @Test
    fun `SelectCurrency converts the displayed price reactively`() =
        runTest(mainDispatcherRule.dispatcher) {
            val vm = viewModel()
            advanceUntilIdle()

            vm.onIntent(DetailIntent.SelectCurrency(Currency.USD))

            val content = vm.state.value as DetailUiState.Content
            assertEquals(Currency.USD, content.selectedCurrency)
            assertEquals(14.18 * 1.088993, content.displayedPrice.amount, 1e-9)
        }

    @Test
    fun `a property absent from the cache yields Error`() =
        runTest(mainDispatcherRule.dispatcher) {
            val vm = viewModel(property = null)
            advanceUntilIdle()

            assertEquals(DetailUiState.Error, vm.state.value)
        }

    @Test
    fun `Retry reloads from Error to Content once the property is available`() =
        runTest(mainDispatcherRule.dispatcher) {
            // id 2 requested but only id 1 cached → not found.
            val vm = DetailViewModel(
                savedStateHandle = SavedStateHandle(mapOf(DetailDestination.ARG_PROPERTY_ID to 1)),
                propertyRepository = FakePropertyRepository(property(id = 1)),
                ratesRepository = FakeRatesRepository(rates),
            )
            advanceUntilIdle()

            vm.state.test {
                assertEquals(DetailUiState.Content::class, awaitItem()::class)
                vm.onIntent(DetailIntent.Retry)
                assertEquals(DetailUiState.Loading, awaitItem())
                assertEquals(DetailUiState.Content::class, awaitItem()::class)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `no rates degrades the toggle to EUR-only`() =
        runTest(mainDispatcherRule.dispatcher) {
            val vm = viewModel(rates = null)
            advanceUntilIdle()

            val content = vm.state.value as DetailUiState.Content
            assertEquals(listOf(Currency.EUR), content.availableCurrencies)
            assertEquals(Money(14.18, Currency.EUR), content.displayedPrice)
        }

    @Test
    fun `a missing rate omits that currency and a selection of it is ignored`() =
        runTest(mainDispatcherRule.dispatcher) {
            val vm = viewModel(rates = rates.copy(rates = rates.rates - Currency.GBP))
            advanceUntilIdle()

            val loaded = vm.state.value as DetailUiState.Content
            assertEquals(listOf(Currency.EUR, Currency.USD), loaded.availableCurrencies)

            // GBP isn't on offer → the selection is a no-op, EUR stays selected.
            vm.onIntent(DetailIntent.SelectCurrency(Currency.GBP))
            val after = vm.state.value as DetailUiState.Content
            assertEquals(Currency.EUR, after.selectedCurrency)
        }
}
