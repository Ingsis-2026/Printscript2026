package version

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class VersionTest {
    @Test
    fun `each version is found by its number`() {
        assertEquals(Version.V1_0, Version.of("1.0"))
        assertEquals(Version.V1_1, Version.of("1.1"))
    }

    @Test
    fun `an unknown number is no version`() {
        assertNull(Version.of("2.0"))
    }

    @Test
    fun `parse rejects an unknown number with the message every module used to give`() {
        val exception = assertThrows<IllegalArgumentException> { Version.parse("2.0") }

        assertEquals("Unsupported version: 2.0", exception.message)
    }

    @Test
    fun `versions are declared oldest first`() {
        assertTrue(Version.V1_0 < Version.V1_1)
    }
}
