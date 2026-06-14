package io.github.rutaleivanpaul.propertylistings.domain.model

/**
 * The currencies the app supports for displaying a property's lowest price.
 *
 * Restricted to the three the brief requires (EUR is the base the API prices in). Keeping this a
 * closed enum — rather than free-form ISO strings — means the price toggle, the converter and the
 * UI all operate on the same exhaustive set, so an unknown code can never leak into the core.
 *
 * @property isoCode the ISO 4217 code as it appears in the rates payload's `rates` map keys.
 */
enum class Currency(val isoCode: String) {
    EUR("EUR"),
    USD("USD"),
    GBP("GBP");

    companion object {
        /** Resolves an ISO code (case-insensitive) to a supported [Currency], or `null` if unsupported. */
        fun fromCode(code: String?): Currency? =
            code?.let { value -> entries.firstOrNull { it.isoCode.equals(value, ignoreCase = true) } }
    }
}
