package io.github.rutaleivanpaul.propertylistings.data.repository

import io.github.rutaleivanpaul.propertylistings.data.remote.api.PropertyApi
import io.github.rutaleivanpaul.propertylistings.data.remote.api.RatesApi
import io.github.rutaleivanpaul.propertylistings.data.remote.api.StatsApi
import io.github.rutaleivanpaul.propertylistings.data.remote.dto.PropertiesResponseDto
import io.github.rutaleivanpaul.propertylistings.data.remote.dto.RatesResponseDto
import io.github.rutaleivanpaul.propertylistings.domain.time.TimeProvider

/**
 * Test doubles shared by the repository tests. Each fake API can be told to return a canned
 * response or to throw a chosen exception, so the four failure modes are reproducible without a
 * network.
 */
class FakePropertyApi(
    var response: PropertiesResponseDto = PropertiesResponseDto(),
    var error: Throwable? = null,
) : PropertyApi {
    var callCount = 0
        private set

    override suspend fun getProperties(): PropertiesResponseDto {
        callCount++
        error?.let { throw it }
        return response
    }
}

class FakeRatesApi(
    var response: RatesResponseDto = RatesResponseDto(),
    var error: Throwable? = null,
) : RatesApi {
    var callCount = 0
        private set

    override suspend fun getRates(): RatesResponseDto {
        callCount++
        error?.let { throw it }
        return response
    }
}

/** Records the telemetry calls the repositories make. */
class RecordingStatsApi : StatsApi {
    val reports = mutableListOf<Pair<String, Long>>()

    override suspend fun report(action: String, durationMillis: Long) {
        reports += action to durationMillis
    }
}

/**
 * A clock returning each supplied value in turn, then sticking on the last. Lets a test assert the
 * measured duration deterministically (e.g. `FakeTimeProvider(100, 350)` → a 250ms request).
 */
class FakeTimeProvider(vararg values: Long) : TimeProvider {
    private val queue = ArrayDeque(values.toList())

    override fun nowMillis(): Long = if (queue.size > 1) queue.removeFirst() else queue.first()
}

/** A clock whose current time can be moved by a test to drive cache-expiry behaviour. */
class MutableTimeProvider(var now: Long = 0L) : TimeProvider {
    override fun nowMillis(): Long = now
}
