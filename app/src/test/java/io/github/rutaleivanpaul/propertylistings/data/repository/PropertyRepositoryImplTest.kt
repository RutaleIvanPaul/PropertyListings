package io.github.rutaleivanpaul.propertylistings.data.repository

import io.github.rutaleivanpaul.propertylistings.data.remote.dto.PropertiesResponseDto
import io.github.rutaleivanpaul.propertylistings.data.stats.StatsReporter
import io.github.rutaleivanpaul.propertylistings.domain.DataResult
import io.github.rutaleivanpaul.propertylistings.util.loadFixture
import io.github.rutaleivanpaul.propertylistings.util.testJson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

/**
 * Unit tests for [PropertyRepositoryImpl] using a fake service, covering all four failure modes,
 * the in-memory cache, forced refresh and telemetry firing.
 *
 * An [UnconfinedTestDispatcher] backs both the repository's IO work and the reporter's scope so the
 * fire-and-forget telemetry runs eagerly and can be asserted without manual scheduler advancing.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PropertyRepositoryImplTest {

    private val sampleResponse: PropertiesResponseDto =
        testJson.decodeFromString(loadFixture("properties_sample.json"))

    private fun TestScope.newRepository(
        api: FakePropertyApi,
        statsApi: RecordingStatsApi,
        time: FakeTimeProvider = FakeTimeProvider(0L),
    ): PropertyRepositoryImpl {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        // Reporter scope on the same eager dispatcher so fire-and-forget telemetry runs inline.
        return PropertyRepositoryImpl(api, StatsReporter(statsApi, CoroutineScope(dispatcher)), time, dispatcher)
    }

    @Test
    fun `success maps valid items, drops bad ones and reports load telemetry`() = runTest {
        val api = FakePropertyApi(response = sampleResponse)
        val statsApi = RecordingStatsApi()
        val repo = newRepository(api, statsApi, time = FakeTimeProvider(100L, 350L))

        val result = repo.getProperties(forceRefresh = false)

        assertTrue(result is DataResult.Success)
        // Fixture has 5 items; the null-id and unparseable-price items are dropped → 3 remain.
        assertEquals(listOf(1, 5575, 42), (result as DataResult.Success).data.map { it.id })
        // Telemetry: one "load" with the measured duration (350 - 100).
        assertEquals(listOf("load" to 250L), statsApi.reports)
    }

    @Test
    fun `network failure maps to NetworkError and reports load-failed with time-to-failure`() = runTest {
        val statsApi = RecordingStatsApi()
        val repo = newRepository(
            FakePropertyApi(error = IOException("no connectivity")),
            statsApi,
            time = FakeTimeProvider(100L, 250L),
        )

        assertEquals(DataResult.NetworkError, repo.getProperties(forceRefresh = false))
        // The failure is still reported, under the distinct label, with the time-to-failure (150).
        assertEquals(listOf("load-failed" to 150L), statsApi.reports)
    }

    @Test
    fun `unparsable body maps to ParseError and also reports load-failed`() = runTest {
        val statsApi = RecordingStatsApi()
        val repo = newRepository(FakePropertyApi(error = SerializationException("bad json")), statsApi)

        assertEquals(DataResult.ParseError, repo.getProperties(forceRefresh = false))
        assertEquals(listOf("load-failed"), statsApi.reports.map { it.first })
    }

    @Test
    fun `unexpected runtime exception degrades to an error result and reports load-failed-unexpected`() = runTest {
        val statsApi = RecordingStatsApi()
        // An exception the boundary doesn't anticipate (not network/parse), like the on-device
        // SecurityException that motivated the defensive catch.
        val repo = newRepository(FakePropertyApi(error = IllegalStateException("unexpected")), statsApi)

        // (a) degrades to an error result rather than propagating out of the repository.
        assertEquals(DataResult.NetworkError, repo.getProperties(forceRefresh = false))
        // (b) fires the distinct unexpected-failure telemetry label.
        assertEquals(listOf("load-failed-unexpected"), statsApi.reports.map { it.first })
    }

    @Test
    fun `empty payload succeeds with an empty list`() = runTest {
        val repo = newRepository(FakePropertyApi(response = PropertiesResponseDto(properties = emptyList())), RecordingStatsApi())

        val result = repo.getProperties(forceRefresh = false)
        assertTrue(result is DataResult.Success && result.data.isEmpty())
    }

    @Test
    fun `second non-forced call serves the cache without a new fetch or report`() = runTest {
        val api = FakePropertyApi(response = sampleResponse)
        val statsApi = RecordingStatsApi()
        val repo = newRepository(api, statsApi)

        repo.getProperties(forceRefresh = false)
        repo.getProperties(forceRefresh = false)

        assertEquals(1, api.callCount)
        assertEquals(1, statsApi.reports.size)
    }

    @Test
    fun `forced refresh bypasses the cache and fetches again`() = runTest {
        val api = FakePropertyApi(response = sampleResponse)
        val repo = newRepository(api, RecordingStatsApi())

        repo.getProperties(forceRefresh = false)
        repo.getProperties(forceRefresh = true)

        assertEquals(2, api.callCount)
    }

    @Test
    fun `cachedProperty returns a loaded item and null before any load`() = runTest {
        val api = FakePropertyApi(response = sampleResponse)
        val repo = newRepository(api, RecordingStatsApi())

        assertNull(repo.cachedProperty(1))

        repo.getProperties(forceRefresh = false)

        assertEquals("Abbey Court Hostel", repo.cachedProperty(1)?.name)
        assertNull(repo.cachedProperty(123456))
    }
}
