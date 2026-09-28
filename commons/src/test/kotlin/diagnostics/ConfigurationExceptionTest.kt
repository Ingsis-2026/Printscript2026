package diagnostics

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

class ConfigurationExceptionTest {
    @Test
    fun `keeps its message and, when there is one, the error that caused it`() {
        val cause = IllegalStateException("malformed")

        val withCause = ConfigurationException("bad rules", cause)
        val withoutCause = ConfigurationException("bad rules")

        assertEquals("bad rules", withCause.message)
        assertSame(cause, withCause.cause)
        assertNull(withoutCause.cause)
    }
}
