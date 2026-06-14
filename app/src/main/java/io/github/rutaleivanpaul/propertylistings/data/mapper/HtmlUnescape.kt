package io.github.rutaleivanpaul.propertylistings.data.mapper

/**
 * Minimal, pure HTML-entity decoder for the entities seen in property `overview` text
 * (e.g. `&#039;` → `'`).
 *
 * Intentionally not `android.text.Html.fromHtml`: that pulls in the Android framework, which would
 * make the mapper untestable on the JVM and is overkill for the handful of entities this data
 * contains. This covers numeric (decimal `&#NN;` and hex `&#xNN;`) and the common named entities;
 * unknown entities are left as-is rather than dropped.
 */
object HtmlUnescape {

    private val numericEntity = Regex("&#(x?)([0-9A-Fa-f]+);")

    private val namedEntities = mapOf(
        "&apos;" to "'",
        "&quot;" to "\"",
        "&lt;" to "<",
        "&gt;" to ">",
        "&nbsp;" to " ",
        // &amp; is applied last so an escaped sequence like "&amp;lt;" is not double-decoded.
        "&amp;" to "&",
    )

    /** Returns [input] with supported HTML entities decoded; non-entity text is unchanged. */
    fun unescape(input: String): String {
        if (input.isEmpty()) return input

        var result = numericEntity.replace(input) { match ->
            val isHex = match.groupValues[1] == "x"
            val digits = match.groupValues[2]
            val code = digits.toIntOrNull(if (isHex) 16 else 10)
            // Leave malformed/out-of-range references untouched rather than throwing.
            if (code != null && code in 1..0x10FFFF) String(Character.toChars(code)) else match.value
        }
        for ((entity, replacement) in namedEntities) {
            result = result.replace(entity, replacement)
        }
        return result
    }
}
