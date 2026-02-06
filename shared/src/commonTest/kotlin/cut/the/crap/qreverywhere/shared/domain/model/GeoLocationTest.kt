package cut.the.crap.qreverywhere.shared.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.assertFails

/**
 * Unit tests for GeoLocation parsing and formatting
 */
class GeoLocationTest {

    // ==================== Format Tests ====================

    @Test
    fun `toGeoUri creates valid geo URI`() {
        val location = GeoLocation(
            latitude = 37.7749,
            longitude = -122.4194
        )

        val result = location.toGeoUri()

        assertTrue(result.startsWith("geo:"))
        assertTrue(result.contains("37.774900"))
        assertTrue(result.contains("-122.419400"))
    }

    @Test
    fun `toGeoUri includes label in query string`() {
        val location = GeoLocation(
            latitude = 48.8584,
            longitude = 2.2945,
            label = "Eiffel Tower"
        )

        val result = location.toGeoUri()

        assertTrue(result.contains("?q=Eiffel%20Tower"))
    }

    @Test
    fun `toGeoUri includes altitude when provided`() {
        val location = GeoLocation(
            latitude = 27.9881,
            longitude = 86.9250,
            altitude = 8848.86 // Mount Everest
        )

        val result = location.toGeoUri()

        // Should have three comma-separated values
        val coords = result.removePrefix("geo:").split(",")
        assertEquals(3, coords.size)
    }

    @Test
    fun `toGeoUri includes uncertainty parameter`() {
        val location = GeoLocation(
            latitude = 37.7749,
            longitude = -122.4194,
            uncertainty = 10.0
        )

        val result = location.toGeoUri()

        assertTrue(result.contains(";u=10.000000"))
    }

    // ==================== Parse Tests ====================

    @Test
    fun `parse extracts coordinates correctly`() {
        val result = GeoLocation.parse("geo:37.7749,-122.4194")

        assertNotNull(result)
        assertEquals(37.7749, result.latitude, 0.0001)
        assertEquals(-122.4194, result.longitude, 0.0001)
    }

    @Test
    fun `parse handles label in query string`() {
        val result = GeoLocation.parse("geo:48.8584,2.2945?q=Eiffel%20Tower")

        assertNotNull(result)
        assertEquals("Eiffel Tower", result.label)
    }

    @Test
    fun `parse handles altitude`() {
        val result = GeoLocation.parse("geo:27.9881,86.9250,8848.86")

        assertNotNull(result)
        assertEquals(8848.86, result.altitude!!, 0.01)
    }

    @Test
    fun `parse handles uncertainty parameter`() {
        val result = GeoLocation.parse("geo:37.7749,-122.4194;u=10")

        assertNotNull(result)
        assertEquals(10.0, result.uncertainty!!, 0.01)
    }

    @Test
    fun `parse returns null for non-geo URI`() {
        val result = GeoLocation.parse("https://maps.google.com")
        assertNull(result)
    }

    @Test
    fun `parse returns null for invalid coordinates`() {
        val result = GeoLocation.parse("geo:invalid,coords")
        assertNull(result)
    }

    @Test
    fun `parse returns null for missing longitude`() {
        val result = GeoLocation.parse("geo:37.7749")
        assertNull(result)
    }

    // ==================== Validation Tests ====================

    @Test
    fun `constructor rejects invalid latitude`() {
        assertFails {
            GeoLocation(latitude = 91.0, longitude = 0.0)
        }
        assertFails {
            GeoLocation(latitude = -91.0, longitude = 0.0)
        }
    }

    @Test
    fun `constructor rejects invalid longitude`() {
        assertFails {
            GeoLocation(latitude = 0.0, longitude = 181.0)
        }
        assertFails {
            GeoLocation(latitude = 0.0, longitude = -181.0)
        }
    }

    // ==================== Display Tests ====================

    @Test
    fun `toDisplayString formats coordinates correctly`() {
        val location = GeoLocation(
            latitude = 37.7749,
            longitude = -122.4194
        )

        val display = location.toDisplayString()

        assertTrue(display.contains("N"))
        assertTrue(display.contains("W"))
    }

    @Test
    fun `toDisplayString includes label when provided`() {
        val location = GeoLocation(
            latitude = 48.8584,
            longitude = 2.2945,
            label = "Eiffel Tower"
        )

        val display = location.toDisplayString()

        assertTrue(display.startsWith("Eiffel Tower"))
    }

    // ==================== Round-Trip Tests ====================

    @Test
    fun `parse and toGeoUri round-trip preserves data`() {
        val original = GeoLocation(
            latitude = 40.7128,
            longitude = -74.0060,
            label = "New York City"
        )

        val geoUri = original.toGeoUri()
        val parsed = GeoLocation.parse(geoUri)

        assertNotNull(parsed)
        assertEquals(original.latitude, parsed.latitude, 0.0001)
        assertEquals(original.longitude, parsed.longitude, 0.0001)
        assertEquals(original.label, parsed.label)
    }

    // ==================== Helper Tests ====================

    @Test
    fun `isGeoUri returns true for valid geo URI`() {
        assertTrue(GeoLocation.isGeoUri("geo:37.7749,-122.4194"))
    }

    @Test
    fun `isGeoUri returns false for non-geo URI`() {
        assertTrue(!GeoLocation.isGeoUri("https://example.com"))
    }
}
