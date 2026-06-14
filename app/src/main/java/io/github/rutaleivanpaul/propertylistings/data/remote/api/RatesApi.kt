package io.github.rutaleivanpaul.propertylistings.data.remote.api

import io.github.rutaleivanpaul.propertylistings.data.remote.dto.RatesResponseDto
import retrofit2.http.GET

/** Retrofit service for the exchange-rates endpoint (commit-less raw gist URL; see DECISIONS.md). */
interface RatesApi {

    @GET("pedrotrabulo-hw/16e87e40ca7b9650aa8e1b936f23e14e/raw/rates.json")
    suspend fun getRates(): RatesResponseDto
}
