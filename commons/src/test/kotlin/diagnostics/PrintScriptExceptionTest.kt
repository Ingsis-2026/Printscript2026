package diagnostics

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import token.TokenPosition

class PrintScriptExceptionTest {
    private class TestException(
        message: String,
        start: TokenPosition? = null,
        end: TokenPosition? = null,
    ) : PrintScriptException(message, start, end)

    @Test
    fun `message stays clean so the location does not leak into it`() {
        val exception = TestException("algo falló", TokenPosition(3, 7), TokenPosition(3, 12))

        assertEquals("algo falló", exception.message)
    }

    @Test
    fun `positions are exposed for the CLI`() {
        val exception = TestException("algo falló", TokenPosition(3, 7), TokenPosition(4, 2))

        assertEquals(TokenPosition(3, 7), exception.startPosition)
        assertEquals(TokenPosition(4, 2), exception.endPosition)
    }

    @Test
    fun `describe reports a span in 1-based coordinates`() {
        val exception = TestException("algo falló", TokenPosition(3, 7), TokenPosition(4, 2))

        assertEquals(
            "algo falló (desde línea 4, columna 8 hasta línea 5, columna 3)",
            exception.describe(),
        )
    }

    @Test
    fun `describe collapses a single point`() {
        val exception = TestException("algo falló", TokenPosition(0, 0), TokenPosition(0, 0))

        assertEquals("algo falló (línea 1, columna 1)", exception.describe())
    }

    @Test
    fun `describe falls back to the start when there is no end`() {
        val exception = TestException("algo falló", TokenPosition(2, 5))

        assertNull(exception.endPosition)
        assertEquals("algo falló (línea 3, columna 6)", exception.describe())
    }

    @Test
    fun `describe returns just the message when there is no position`() {
        val exception = TestException("algo falló")

        assertEquals("algo falló", exception.describe())
    }
}
