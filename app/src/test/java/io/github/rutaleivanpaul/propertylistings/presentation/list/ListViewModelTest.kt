package io.github.rutaleivanpaul.propertylistings.presentation.list

import app.cash.turbine.test
import io.github.rutaleivanpaul.propertylistings.domain.DataResult
import io.github.rutaleivanpaul.propertylistings.domain.model.Currency
import io.github.rutaleivanpaul.propertylistings.domain.model.Money
import io.github.rutaleivanpaul.propertylistings.domain.model.Property
import io.github.rutaleivanpaul.propertylistings.domain.model.PropertyType
import io.github.rutaleivanpaul.propertylistings.domain.repository.PropertyRepository
import io.github.rutaleivanpaul.propertylistings.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for [ListViewModel] state transitions per intent, including the error, empty and
 * refresh paths, plus the navigation and refresh-failure effects. Uses a fake repository (so the
 * ViewModel is tested against the abstraction) and Turbine to assert the StateFlow/effect streams.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private class FakePropertyRepository : PropertyRepository {
        var result: DataResult<List<Property>> = DataResult.Success(emptyList())
        val forceRefreshCalls = mutableListOf<Boolean>()

        override suspend fun getProperties(forceRefresh: Boolean): DataResult<List<Property>> {
            forceRefreshCalls += forceRefresh
            return result
        }

        override fun cachedProperty(id: Int): Property? = null
    }

    private fun property(id: Int = 1, city: String = "Dublin") = Property(
        id = id,
        name = "Hostel $id",
        isFeatured = false,
        ratingOutOf10 = 8.7,
        numberOfRatings = 100,
        price = Money(14.18, Currency.EUR),
        overview = "",
        type = PropertyType.HOSTEL,
        city = city,
        country = "Ireland",
        imageUrls = emptyList(),
    )

    @Test
    fun `on init, emits Loading then Content for a successful non-empty load`() =
        runTest(mainDispatcherRule.dispatcher) {
            val repo = FakePropertyRepository().apply { result = DataResult.Success(listOf(property())) }
            val viewModel = ListViewModel(repo)

            viewModel.state.test {
                assertEquals(ListUiState.Loading, awaitItem())
                assertEquals(ListUiState.Content(listOf(property())), awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `on init, a successful empty load emits Empty`() =
        runTest(mainDispatcherRule.dispatcher) {
            val repo = FakePropertyRepository().apply { result = DataResult.Success(emptyList()) }
            val viewModel = ListViewModel(repo)

            viewModel.state.test {
                assertEquals(ListUiState.Loading, awaitItem())
                assertEquals(ListUiState.Empty, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `on init, a failed load emits Error`() =
        runTest(mainDispatcherRule.dispatcher) {
            val repo = FakePropertyRepository().apply { result = DataResult.NetworkError }
            val viewModel = ListViewModel(repo)

            viewModel.state.test {
                assertEquals(ListUiState.Loading, awaitItem())
                assertEquals(ListUiState.Error, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `Retry after an error reloads to Content and forces a refresh`() =
        runTest(mainDispatcherRule.dispatcher) {
            val repo = FakePropertyRepository().apply { result = DataResult.NetworkError }
            val viewModel = ListViewModel(repo)
            advanceUntilIdle() // settle on Error

            repo.result = DataResult.Success(listOf(property()))

            viewModel.state.test {
                assertEquals(ListUiState.Error, awaitItem()) // current
                viewModel.onIntent(ListIntent.Retry)
                assertEquals(ListUiState.Loading, awaitItem())
                assertEquals(ListUiState.Content(listOf(property())), awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
            // init load (false) + retry (true).
            assertEquals(listOf(false, true), repo.forceRefreshCalls)
        }

    @Test
    fun `Refresh shows isRefreshing over existing content, then updates`() =
        runTest(mainDispatcherRule.dispatcher) {
            val repo = FakePropertyRepository().apply { result = DataResult.Success(listOf(property())) }
            val viewModel = ListViewModel(repo)
            advanceUntilIdle() // settle on Content

            val refreshed = listOf(property(), property(id = 2))
            repo.result = DataResult.Success(refreshed)

            viewModel.state.test {
                assertEquals(ListUiState.Content(listOf(property())), awaitItem()) // current
                viewModel.onIntent(ListIntent.Refresh)
                assertEquals(ListUiState.Content(listOf(property()), isRefreshing = true), awaitItem())
                assertEquals(ListUiState.Content(refreshed), awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
            assertEquals(listOf(false, true), repo.forceRefreshCalls) // init + refresh(force)
        }

    @Test
    fun `Refresh failure keeps last-good content and emits a refresh-error effect`() =
        runTest(mainDispatcherRule.dispatcher) {
            val repo = FakePropertyRepository().apply { result = DataResult.Success(listOf(property())) }
            val viewModel = ListViewModel(repo)
            advanceUntilIdle() // settle on Content

            viewModel.effects.test {
                repo.result = DataResult.NetworkError
                viewModel.onIntent(ListIntent.Refresh)
                advanceUntilIdle()

                assertEquals(ListEffect.ShowRefreshError, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
            // Content is retained (not replaced by Error) after the failed refresh.
            assertEquals(ListUiState.Content(listOf(property())), viewModel.state.value)
        }

    @Test
    fun `SelectProperty emits a navigate-to-detail effect`() =
        runTest(mainDispatcherRule.dispatcher) {
            val repo = FakePropertyRepository().apply { result = DataResult.Success(listOf(property())) }
            val viewModel = ListViewModel(repo)
            advanceUntilIdle()

            viewModel.effects.test {
                viewModel.onIntent(ListIntent.SelectProperty(id = 42))
                advanceUntilIdle()

                assertEquals(ListEffect.NavigateToDetail(42), awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `the no-rating sentinel survives the round-trip into Content`() =
        runTest(mainDispatcherRule.dispatcher) {
            val noRating = property().copy(ratingOutOf10 = 0.0)
            val repo = FakePropertyRepository().apply { result = DataResult.Success(listOf(noRating)) }
            val viewModel = ListViewModel(repo)
            advanceUntilIdle()

            val content = viewModel.state.value as ListUiState.Content
            assertTrue(!content.properties.first().hasRating)
        }
}
