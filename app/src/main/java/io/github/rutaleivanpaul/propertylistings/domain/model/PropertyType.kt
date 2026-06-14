package io.github.rutaleivanpaul.propertylistings.domain.model

/**
 * The kind of accommodation a property is.
 *
 * The API sends a free-text `type` (e.g. "HOSTEL"); it is normalised to this closed set at the
 * mapping boundary. [OTHER] absorbs any value that is missing or unrecognised, so the detail
 * screen always has a sensible label to show without leaking raw API strings into the UI.
 */
enum class PropertyType {
    HOSTEL,
    GUESTHOUSE,
    HOTEL,
    OTHER;

    companion object {
        /** Maps an API `type` string (case-insensitive) to a [PropertyType], defaulting to [OTHER]. */
        fun fromApi(raw: String?): PropertyType =
            entries.firstOrNull { it.name.equals(raw?.trim(), ignoreCase = true) } ?: OTHER
    }
}
