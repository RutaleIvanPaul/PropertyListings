package io.github.rutaleivanpaul.propertylistings.presentation.common

import io.github.rutaleivanpaul.propertylistings.domain.model.PropertyType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Guards that every [PropertyType] maps to a distinct, real label resource. */
class PropertyTypeLabelTest {

    @Test
    fun `every type maps to a non-zero resource`() {
        PropertyType.entries.forEach { type ->
            assertTrue("missing label for $type", type.labelRes() != 0)
        }
    }

    @Test
    fun `each type maps to a distinct resource`() {
        val ids = PropertyType.entries.map { it.labelRes() }
        assertEquals(ids.size, ids.toSet().size)
        assertNotEquals(PropertyType.HOSTEL.labelRes(), PropertyType.HOTEL.labelRes())
    }
}
