package io.github.rutaleivanpaul.propertylistings.domain.repository

import io.github.rutaleivanpaul.propertylistings.domain.DataResult
import io.github.rutaleivanpaul.propertylistings.domain.model.Property

/**
 * Source of property data for the presentation layer.
 *
 * An interface so ViewModels depend on the abstraction (testable with a fake) while Hilt supplies
 * the networked implementation, and so an alternative source (cached, fake) is substitutable.
 */
interface PropertyRepository {

    /**
     * Loads the property list.
     *
     * @param forceRefresh when `false`, an in-memory last-good list (if any) is returned without a
     *   network call; when `true` (pull-to-refresh) a fresh fetch is always attempted.
     * @return [DataResult.Success] with the mapped properties, or a distinct failure result.
     */
    suspend fun getProperties(forceRefresh: Boolean = false): DataResult<List<Property>>

    /**
     * The property with [id] from the last successful load, or `null` if not cached.
     *
     * Lets the detail screen reuse the data already fetched for the list instead of issuing another
     * request, which is why the only network call the detail flow makes is for exchange rates.
     */
    fun cachedProperty(id: Int): Property?
}
