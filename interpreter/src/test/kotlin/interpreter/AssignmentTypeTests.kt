package interpreter

import ast.AssignationNode
import ast.LiteralNode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import token.TokenPosition
import token.TokenType

/**
 * Reasignación de variables de todo tipo soportado, no sólo Int y String: un booleano y un
 * decimal deben poder reasignarse, y el chequeo de tipos debe seguir rechazando la mezcla.
 */
class AssignmentTypeTests {
    private val position = TokenPosition(0, 0)
    private val printer =
        object : Printer {
            override fun print(message: String) = Unit
        }
    private val reader =
        object : Reader {
            override fun input(message: String): String = ""
        }

    private fun assignation(
        id: String,
        value: String,
        type: TokenType,
    ) = AssignationNode(id, LiteralNode(value, type, position), type, position)

    @Test
    fun `a boolean variable can be reassigned to another boolean`() {
        val interpreter = Interpreter(printer, reader)
        interpreter.execute(assignation("flag", "true", TokenType.BOOLEANLITERAL))

        val result = interpreter.execute(assignation("flag", "false", TokenType.BOOLEANLITERAL))

        assertEquals(false, result)
        assertEquals(false, interpreter.variables["flag"])
    }

    @Test
    fun `a decimal variable can be reassigned to another decimal`() {
        val interpreter = Interpreter(printer, reader)
        interpreter.variables["ratio"] = 1.5

        val result = interpreter.execute(assignation("ratio", "2.5", TokenType.NUMBERLITERAL))

        assertEquals(2.5, result)
    }

    @Test
    fun `reassigning a boolean with a string still fails`() {
        val interpreter = Interpreter(printer, reader)
        interpreter.execute(assignation("flag", "true", TokenType.BOOLEANLITERAL))

        val exception =
            assertThrows<InterpreterException> {
                interpreter.execute(assignation("flag", "texto", TokenType.STRINGLITERAL))
            }
        assertEquals("Invalid expression for type booleanliteral", exception.message)
    }

    @Test
    fun `assignment no longer writes debug output to stdout`() {
        val interpreter = Interpreter(printer, reader)
        val outputStream = java.io.ByteArrayOutputStream()
        val original = System.out
        try {
            System.setOut(java.io.PrintStream(outputStream))
            interpreter.execute(assignation("x", "30", TokenType.NUMBERLITERAL))
        } finally {
            System.setOut(original)
        }

        assertEquals("", outputStream.toString().trim())
    }
}
