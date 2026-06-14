package io.github.rutaleivanpaul.propertylistings.data.repository

import io.github.rutaleivanpaul.propertylistings.data.mapper.PropertyMapper
import io.github.rutaleivanpaul.propertylistings.data.remote.api.PropertyApi
import io.github.rutaleivanpaul.propertylistings.data.stats.StatsAction
import io.github.rutaleivanpaul.propertylistings.data.stats.StatsReporter
import io.github.rutaleivanpaul.propertylistings.di.IoDispatcher
import io.github.rutaleivanpaul.propertylistings.domain.DataResult
import io.github.rutaleivanpaul.propertylistings.domain.model.Property
import io.github.rutaleivanpaul.propertylistings.domain.repository.PropertyRepository
import io.github.rutaleivanpaul.propertylistings.domain.time.TimeProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
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
            try {
                val response = propertyApi.getProperties()
                val properties = PropertyMapper.map(response)
                cache = properties
                // Report only on a real, successful fetch; measured request-start → fully-parsed.
                statsReporter.report(StatsAction.LOAD, timeProvider.nowMillis() - startMillis)
                DataResult.Success(properties)
            } catch (e: CancellationException) {
                throw e
            } catch (e: SerializationException) {
                DataResult.ParseError
            } catch (e: Exception) {
                DataResult.NetworkError
            }
        }
    }

    override fun cachedProperty(id: Int): Property? = cache?.firstOrNull { it.id == id }
}
