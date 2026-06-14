package io.github.rutaleivanpaul.propertylistings.data.mapper

import org.junit.Assert.assertEquals
import org.junit.Test

/** Unit tests for [HtmlUnescape] covering numeric, hex and named entities, and pass-through text. */
class HtmlUnescapeTest {

    @Test
    fun `decodes decimal numeric entity`() {
        assertEquals("Dublin's hostel", HtmlUnescape.unescape("Dublin&#039;s hostel"))
    }

    @Test
    fun `decodes hex numeric entity`() {
        assertEquals("'", HtmlUnescape.unescape("&#x27;"))
    }

    @Test
    fun `decodes common named entities`() {
        assertEquals("bed & breakfast < > \"q\"", HtmlUnescape.unescape("bed &amp; breakfast &lt; &gt; &quot;q&quot;"))
    }

    @Test
    fun `does not double-decode an escaped ampersand sequence`() {
        // "&amp;lt;" is a literal "&lt;", not a decoded "<".
        assertEquals("&lt;", HtmlUnescape.unescape("&amp;lt;"))
    }

    @Test
    fun `leaves plain text and unknown entities untouched`() {
        assertEquals("plain text &unknown;", HtmlUnescape.unescape("plain text &unknown;"))
    }

    @Test
    fun `empty string is returned unchanged`() {
        assertEquals("", HtmlUnescape.unescape(""))
    }
}
