package io.github.rutaleivanpaul.propertylistings.domain.repository

import io.github.rutaleivanpaul.propertylistings.domain.model.Rates

/**
 * Source of exchange rates for the detail screen's currency toggle.
 *
 * Separate from [PropertyRepository] because rates have a different lifecycle (fetched on demand,
 * short-lived cache) and a different failure policy (degrade to EUR-only rather than error).
 */
interface RatesRepository {

    /**
     * Returns exchange rates, preferring a fresh in-memory cache and falling back gracefully.
     *
     * Contract:
     * - within the cache TTL → returns the cached rates with no network call;
     * - expired/empty → fetches fresh and caches the result;
     * - on fetch failure → returns the last-good cached rates if any;
     * - if nothing has ever been cached → returns `null`, signalling the toggle should degrade to
     *   showing the EUR price only.
     */
    suspend fun getRates(): Rates?
}
