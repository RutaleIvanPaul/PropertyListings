package io.github.rutaleivanpaul.propertylistings.data.repository

import io.github.rutaleivanpaul.propertylistings.data.mapper.PropertyMapper
import io.github.rutaleivanpaul.propertylistings.data.remote.api.PropertyApi
import io.github.rutaleivanpaul.propertylistings.data.stats.StatsAction
import io.github.rutaleivanpaul.propertylistings.data.stats.StatsReporter
import io.github.rutaleivanpaul.propertylistings.di.IoDispatcher
import io.github.rutaleivanpaul.propertylistings.domain.DataResult
import io.github.rutaleivanpaul.propertylistings.domain.model.Property
import android.util.Log
import io.github.rutaleivanpaul.propertylistings.domain.repository.PropertyRepository
import io.github.rutaleivanpaul.propertylistings.domain.time.TimeProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

/**
 * Networked [PropertyRepository] with an in-memory last-good cache.
 *
 * The cache serves two purposes: it lets the detail screen reuse list data (see [cachedProperty]),
 * and it lets a non-forced [getProperties] return instantly without a network call. Pull-to-refresh
 * passes `forceRefresh = true` to bypass it.
 *
 * Failure handling maps exceptions to the two repository-level failure modes: a deserialization
 * failure becomes [DataResult.ParseError]; anything else (connectivity, timeout, HTTP error)
 * becomes [DataResult.NetworkError]. Per-item missing/corrupt data is handled earlier by
 * [PropertyMapper] and never reaches here.
 */
@Singleton
class PropertyRepositoryImpl @Inject constructor(
    private val propertyApi: PropertyApi,
    private val statsReporter: StatsReporter,
    private val timeProvider: TimeProvider,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : PropertyRepository {

    /** Last successful list. Volatile because it is read/written across coroutine dispatches. */
    @Volatile
    private var cache: List<Property>? = null

    override suspend fun getProperties(forceRefresh: Boolean): DataResult<List<Property>> {
        cache?.let { cached ->
            if (!forceRefresh) return DataResult.Success(cached)
        }

        return withContext(ioDispatcher) {
            val startMillis = timeProvider.nowMillis()
            var action = StatsAction.LOAD
            val result = try {
                val response = propertyApi.getProperties()
                val properties = PropertyMapper.map(response)
                cache = properties
                DataResult.Success(properties)
            } catch (e: CancellationException) {
                // Normal coroutine cancellation, not a request failure — propagate, do not report.
                throw e
            } catch (e: SerializationException) {
                action = StatsAction.LOAD_FAILED
                DataResult.ParseError
            } catch (e: IOException) {
                action = StatsAction.LOAD_FAILED
                DataResult.NetworkError
            } catch (e: HttpException) {
                action = StatsAction.LOAD_FAILED
                DataResult.NetworkError
            } catch (e: Exception) {
                // Defensive boundary catch: an unforeseen exception degrades to the error state
                // instead of crashing. Fail soft for the user, loud for the developer — log at ERROR
                // with the full stack trace (a caught exception does NOT auto-dump like a crash), and
                // report a distinct telemetry label so it isn't swallowed among normal failures. In
                // production this is where we'd also record a non-fatal (e.g. Crashlytics.recordException).
                Log.e(TAG, "Unexpected failure loading properties; degrading to error state.", e)
                action = StatsAction.LOAD_FAILED_UNEXPECTED
                DataResult.NetworkError
            }
            // Telemetry is a side effect: report the outcome under its label, measured to the moment
            // the call resolved (time-to-parsed on success, time-to-failure on failure). The
            // DataResult still propagates to the caller unchanged.
            statsReporter.report(action, timeProvider.nowMillis() - startMillis)
            result
        }
    }

    private companion object {
        const val TAG = "PropertyRepository"
    }

    override fun cachedProperty(id: Int): Property? = cache?.firstOrNull { it.id == id }
}
