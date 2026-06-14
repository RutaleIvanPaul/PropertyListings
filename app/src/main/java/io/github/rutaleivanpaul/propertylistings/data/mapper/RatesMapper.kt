package io.github.rutaleivanpaul.propertylistings.data.mapper

import io.github.rutaleivanpaul.propertylistings.data.remote.dto.RatesResponseDto
import io.github.rutaleivanpaul.propertylistings.domain.model.Currency
import io.github.rutaleivanpaul.propertylistings.domain.model.Rates

/**
 * Maps the tolerant [RatesResponseDto] to the strict domain [Rates].
 *
 * Only currencies the app supports are retained; unknown ISO codes are discarded so the core never
 * holds rates it cannot use. An unrecognised or absent `base` defaults to EUR — the base the API
 * always publishes and the base property prices are quoted in.
 */
object RatesMapper {

    fun map(dto: RatesResponseDto): Rates {
        val base = Currency.fromCode(dto.base) ?: Currency.EUR
        val supported = dto.rates.orEmpty().mapNotNull { (code, value) ->
            Currency.fromCode(code)?.let { it to value }
        }.toMap()
        return Rates(base = base, rates = supported)
    }
}
