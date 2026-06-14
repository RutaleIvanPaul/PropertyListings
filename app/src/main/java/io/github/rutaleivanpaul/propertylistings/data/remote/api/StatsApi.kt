package io.github.rutaleivanpaul.propertylistings.data.remote.api

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit service for the telemetry/stats endpoint.
 *
 * The app reports client-perceived request durations here and ignores the (static `0`) response.
 *
 * NOTE: a GET with query parameters is used because that is the shape the provided endpoint
 * accepts. A production telemetry pipeline would instead POST a batched payload (multiple
 * measurements, structured body, retry/back-pressure) rather than one fire-and-forget GET per
 * request. See README.
 */
interface StatsApi {

    @GET("pedrotrabulo-hw/6bed011203c6c8217f0d55f74ddcc5c5/raw/stats")
    suspend fun report(
        @Query("action") action: String,
        @Query("duration") durationMillis: Long,
    )
}
