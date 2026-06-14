package io.github.rutaleivanpaul.propertylistings.data.repository

import io.github.rutaleivanpaul.propertylistings.data.mapper.RatesMapper
import io.github.rutaleivanpaul.propertylistings.data.remote.api.RatesApi
import io.github.rutaleivanpaul.propertylistings.data.stats.StatsAction
import io.github.rutaleivanpaul.propertylistings.data.stats.StatsReporter
import io.github.rutaleivanpaul.propertylistings.di.IoDispatcher
import io.github.rutaleivanpaul.propertylistings.domain.model.Rates
import io.github.rutaleivanpaul.propertylistings.domain.repository.RatesRepository
import io.github.rutaleivanpaul.propertylistings.domain.time.TimeProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * Networked [RatesRepository] with an in-memory, timestamped TTL cache and last-good fallback.
 *
 * Rates are fetched on demand (no background warm-up — deferred; see DECISIONS.md). Within the TTL
 * the cached value is reused without a network call; once expired it is refetched. A failed refetch
 * falls back to the last-good cache, and if nothing has ever been cached, `null` is returned so the
 * detail screen degrades to showing the EUR price only.
 *
 * The clock is injected ([TimeProvider]) so expiry is unit-testable without real delays.
 */
@Singleton
class RatesRepositoryImpl @Inject constructor(
    private val ratesApi: RatesApi,
    private val statsReporter: StatsReporter,
    private val timeProvider: TimeProvider,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : RatesRepository {

    /** Rates plus the wall-clock time they were fetched, used to compute freshness against the TTL. */
    private data class CachedRates(val rates: Rates, val fetchedAtMillis: Long)

    @Volatile
    private var cache: CachedRates? = null

    override suspend fun getRates(): Rates? {
        cache?.let { cached ->
            val age = timeProvider.nowMillis() - cached.fetchedAtMillis
            if (age < RATES_CACHE_TTL.inWholeMilliseconds) return cached.rates
        }

        return withContext(ioDispatcher) {
            val startMillis = timeProvider.nowMillis()
            try {
                val response = ratesApi.getRates()
                val rates = RatesMapper.map(response)
                cache = CachedRates(rates, timeProvider.nowMillis())
                statsReporter.report(StatsAction.LOAD_DETAILS, timeProvider.nowMillis() - startMillis)
                rates
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Last-good fallback; null if nothing has ever been cached (caller degrades to EUR).
                cache?.rates
            }
        }
    }

    private companion object {
        /**
         * Exchange rates are published roughly daily, so a short window keeps prices
         * current within a session while avoiding a refetch on rapid back-and-forth
         * navigation between the list and detail screens.
         */
        val RATES_CACHE_TTL: Duration = 5.minutes
    }
}
