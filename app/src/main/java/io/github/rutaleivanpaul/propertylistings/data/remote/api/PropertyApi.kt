package io.github.rutaleivanpaul.propertylistings.data.remote.api

import io.github.rutaleivanpaul.propertylistings.data.remote.dto.PropertiesResponseDto
import retrofit2.http.GET

/**
 * Retrofit service for the properties endpoint.
 *
 * The path is the commit-less raw gist URL (latest revision) for the renamed `pedrotrabulo-hw`
 * account; see DECISIONS.md for why the brief's commit-pinned URLs are avoided.
 */
interface PropertyApi {

    @GET("pedrotrabulo-hw/a1517b9da90dd6877385a65f324ffbc3/raw/properties.json")
    suspend fun getProperties(): PropertiesResponseDto
}
