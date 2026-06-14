package io.github.rutaleivanpaul.propertylistings.data.stats

import io.github.rutaleivanpaul.propertylistings.data.remote.api.StatsApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

/**
 * Unit tests for [StatsReporter]: it forwards the right label/duration and swallows failures so a
 * broken telemetry endpoint can never crash or block the caller.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class StatsReporterTest {

    private class FakeStatsApi(private val shouldThrow: Boolean = false) : StatsApi {
        var callCount = 0
        var lastAction: String? = null
        var lastDuration: Long? = null

        override suspend fun report(action: String, durationMillis: Long) {
            callCount++
            lastAction = action
            lastDuration = durationMillis
            if (shouldThrow) throw IOException("stats endpoint down")
        }
    }

    @Test
    fun `report forwards the action label and duration`() = runTest {
        val api = FakeStatsApi()
        StatsReporter(api, this).report(StatsAction.LOAD, durationMillis = 123L)

        advanceUntilIdle()

        assertEquals("load", api.lastAction)
        assertEquals(123L, api.lastDuration)
    }

    @Test
    fun `a failing endpoint is swallowed silently and does not propagate`() = runTest {
        val api = FakeStatsApi(shouldThrow = true)
        StatsReporter(api, this).report(StatsAction.LOAD_DETAILS, durationMillis = 5L)

        // Must complete without throwing despite the API error.
        advanceUntilIdle()

        assertEquals(1, api.callCount)
        assertEquals("load-details", api.lastAction)
    }
}
