package io.github.rutaleivanpaul.propertylistings.data.repository

import io.github.rutaleivanpaul.propertylistings.data.remote.dto.RatesResponseDto
import io.github.rutaleivanpaul.propertylistings.data.stats.StatsReporter
import io.github.rutaleivanpaul.propertylistings.domain.model.Currency
import io.github.rutaleivanpaul.propertylistings.util.loadFixture
import io.github.rutaleivanpaul.propertylistings.util.testJson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.IOException

/**
 * Unit tests for [RatesRepositoryImpl]: the TTL cache (reuse within TTL, refetch after), the
 * last-good fallback on failure, the EUR-only degrade when nothing is cached, and telemetry.
 *
 * Time is driven by a [MutableTimeProvider] so cache expiry is exercised without real delays.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RatesRepositoryImplTest {

    /** 5-minute TTL from the repository, in milliseconds. */
    private val ttlMillis = 5 * 60 * 1000L

    private val sampleResponse: RatesResponseDto =
        testJson.decodeFromString(loadFixture("rates_sample.json"))

    private fun TestScope.newRepository(
        api: FakeRatesApi,
        statsApi: RecordingStatsApi,
        time: MutableTimeProvider,
    ): RatesRepositoryImpl {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        // Reporter scope on the same eager dispatcher so fire-and-forget telemetry runs inline.
        return RatesRepositoryImpl(api, StatsReporter(statsApi, CoroutineScope(dispatcher)), time, dispatcher)
    }

    @Test
    fun `first call fetches, caches and reports load-details`() = runTest {
        val api = FakeRatesApi(response = sampleResponse)
        val statsApi = RecordingStatsApi()
        val repo = newRepository(api, statsApi, MutableTimeProvider(0L))

        val rates = repo.getRates()

        assertEquals(Currency.EUR, rates?.base)
        assertEquals(1.088993, rates?.rates?.get(Currency.USD)!!, 0.0)
        assertEquals(1, api.callCount)
        assertEquals(1, statsApi.reports.size)
        assertEquals("load-details", statsApi.reports.first().first)
    }

    @Test
    fun `within TTL the cache is reused without a fetch or report`() = runTest {
        val api = FakeRatesApi(response = sampleResponse)
        val statsApi = RecordingStatsApi()
        val time = MutableTimeProvider(0L)
        val repo = newRepository(api, statsApi, time)

        repo.getRates()
        time.now = ttlMillis - 1 // still fresh
        repo.getRates()

        assertEquals(1, api.callCount)
        assertEquals(1, statsApi.reports.size)
    }

    @Test
    fun `after the TTL expires the rates are refetched`() = runTest {
        val api = FakeRatesApi(response = sampleResponse)
        val statsApi = RecordingStatsApi()
        val time = MutableTimeProvider(0L)
        val repo = newRepository(api, statsApi, time)

        repo.getRates()
        time.now = ttlMillis + 1 // expired
        repo.getRates()

        assertEquals(2, api.callCount)
        assertEquals(2, statsApi.reports.size)
    }

    @Test
    fun `a failed refetch falls back to the last-good cache and reports load-details-failed`() = runTest {
        val api = FakeRatesApi(response = sampleResponse)
        val statsApi = RecordingStatsApi()
        val time = MutableTimeProvider(0L)
        val repo = newRepository(api, statsApi, time)

        val fresh = repo.getRates()
        api.error = IOException("rates endpoint down")
        time.now = ttlMillis + 1 // force a refetch, which will now fail

        val fallback = repo.getRates()

        assertEquals(fresh, fallback)
        // The successful fetch reports load-details; the failed refetch reports load-details-failed.
        assertEquals(listOf("load-details", "load-details-failed"), statsApi.reports.map { it.first })
    }

    @Test
    fun `a failure with nothing cached returns null and reports load-details-failed`() = runTest {
        val api = FakeRatesApi(error = IOException("rates endpoint down"))
        val statsApi = RecordingStatsApi()
        val repo = newRepository(api, statsApi, MutableTimeProvider(0L))

        assertNull(repo.getRates())
        assertEquals(listOf("load-details-failed"), statsApi.reports.map { it.first })
    }

    @Test
    fun `an unexpected runtime exception degrades gracefully and reports load-details-failed-unexpected`() = runTest {
        val api = FakeRatesApi(error = IllegalStateException("unexpected"))
        val statsApi = RecordingStatsApi()
        val repo = newRepository(api, statsApi, MutableTimeProvider(0L))

        // Degrades to EUR-only (null) rather than propagating, and flags the unexpected signal.
        assertNull(repo.getRates())
        assertEquals(listOf("load-details-failed-unexpected"), statsApi.reports.map { it.first })
    }
}
