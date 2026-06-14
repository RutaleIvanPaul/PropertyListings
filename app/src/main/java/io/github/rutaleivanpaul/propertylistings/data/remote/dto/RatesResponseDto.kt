package io.github.rutaleivanpaul.propertylistings.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Tolerant DTO for the fixer-style `rates.json` payload.
 *
 * `rates` is a flat map of ISO code → units per one unit of [base]. Only the codes the app supports
 * are retained when this is mapped to the domain [io.github.rutaleivanpaul.propertylistings.domain.model.Rates];
 * unknown codes are discarded at that boundary.
 */
@Serializable
data class RatesResponseDto(
    val base: String? = null,
    val date: String? = null,
    val rates: Map<String, Double>? = null,
)
