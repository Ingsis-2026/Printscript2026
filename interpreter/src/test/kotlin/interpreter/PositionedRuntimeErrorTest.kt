package interpreter

import lexer.Lexer
import lexer.TokenMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import parser.Parser

/**
 * La consigna pide informar la ubicación de los errores semánticos, no sólo su mensaje.
 */
class PositionedRuntimeErrorTest {
    private val printer =
        object : Printer {
            override fun print(message: String) = Unit
        }
    private val reader =
        object : Reader {
            override fun input(message: String): String = ""
        }

    private fun run(source: String) {
        val nodes = Parser.forVersion("1.0").execute(Lexer(TokenMapper("1.0")).execute(source))
        val interpreter = Interpreter.forVersion("1.0", printer, reader)
        nodes.forEach { interpreter.execute(it) }
    }

    @Test
    fun `an undefined variable reports where it was used`() {
        val exception = assertThrows<InterpreterException> { run("println(missing);") }

        assertEquals("Undefined variable: missing", exception.message)
        assertNotNull(exception.startPosition, "El error debe traer su ubicación")
        assertEquals(0, exception.startPosition?.row)
    }

    @Test
    fun `a division by zero reports its line`() {
        val source =
            """
            let a : number = 1;
            let b : number = 0;
            println(a / b);
            """.trimIndent()

        val exception = assertThrows<InterpreterException> { run(source) }

        assertEquals("Division by zero", exception.message)
        assertEquals(2, exception.startPosition?.row, "La división está en la tercera línea")
    }

    @Test
    fun `a type mismatch on reassignment reports its line`() {
        val source =
            """
            let a : number = 1;
            a = "texto";
            """.trimIndent()

        val exception = assertThrows<InterpreterException> { run(source) }

        assertTrue(exception.message!!.contains("Invalid expression for type"), exception.message)
        assertEquals(1, exception.startPosition?.row, "La reasignación está en la segunda línea")
    }

    @Test
    fun `describe includes both the message and the location`() {
        val exception = assertThrows<InterpreterException> { run("println(missing);") }

        val described = exception.describe()
        assertTrue(described.contains("Undefined variable: missing"), described)
        assertTrue(described.contains("línea 1"), described)
    }

    @Test
    fun `the innermost failing node supplies the position`() {
        val source =
            """
            let a : number = 1;
            let b : number = 2;
            println(a + b + missing);
            """.trimIndent()

        val exception = assertThrows<InterpreterException> { run(source) }

        assertEquals("Undefined variable: missing", exception.message)
        assertEquals(2, exception.startPosition?.row)
        // La columna debe apuntar al identificador, no al comienzo de la sentencia.
        assertTrue(
            exception.startPosition!!.column > 8,
            "Se esperaba la columna de 'missing', fue ${exception.startPosition}",
        )
    }
}
