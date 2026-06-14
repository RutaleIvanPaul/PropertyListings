package io.github.rutaleivanpaul.propertylistings.util

import kotlinx.serialization.json.Json

/** The JSON parser configured exactly as the app's, so fixture tests exercise the real settings. */
val testJson: Json = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
}

/** Loads a JSON fixture from `src/test/resources/fixtures/` by file name. */
fun loadFixture(fileName: String): String =
    requireNotNull(object {}.javaClass.getResource("/fixtures/$fileName")) {
        "Missing test fixture: fixtures/$fileName"
    }.readText()
