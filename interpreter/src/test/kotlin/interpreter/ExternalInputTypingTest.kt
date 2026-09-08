package interpreter

import ast.FunctionNode
import ast.LiteralNode
import ast.PrintNode
import lexer.Lexer
import lexer.TokenMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import parser.Parser
import token.TokenPosition
import token.TokenType

/**
 * `readInput` y `readEnv` devuelven un tipo compatible con el de la variable que recibe el
 * resultado, y la ejecución falla si el valor no puede interpretarse como ese tipo.
 */
class ExternalInputTypingTest {
    private val outputs = mutableListOf<String>()

    private val printer =
        object : Printer {
            override fun print(message: String) {
                outputs.add(message)
            }
        }

    private fun readerOf(vararg lines: String): Reader {
        val pending = ArrayDeque(lines.toList())
        return object : Reader {
            override fun input(message: String): String = pending.removeFirst()
        }
    }

    private fun run(
        source: String,
        vararg input: String,
    ): Interpreter {
        val interpreter = Interpreter.forVersion("1.1", printer, readerOf(*input))
        val tokens = Lexer(TokenMapper.forVersion("1.1")).execute(source)
        Parser.forVersion("1.1").execute(tokens).forEach { interpreter.execute(it) }
        return interpreter
    }

    @Test
    fun `a string variable keeps the text as it was typed`() {
        val interpreter = run("let s: string = readInput(\"v: \");", "5")

        assertEquals("5", interpreter.variables["s"])
    }

    @Test
    fun `a number variable receives a number`() {
        val interpreter = run("let n: number = readInput(\"v: \");", "42")

        assertEquals(42, interpreter.variables["n"])
    }

    @Test
    fun `a boolean variable receives a boolean`() {
        val interpreter = run("let b: boolean = readInput(\"v: \");", "true")

        assertEquals(true, interpreter.variables["b"])
    }

    @Test
    fun `a value that cannot be read as the declared type fails`() {
        val exception =
            assertThrows<InterpreterException> {
                run("let b: boolean = readInput(\"v: \");", "Hola")
            }

        assertEquals("readInput devolvió \"Hola\", que no puede interpretarse como boolean", exception.message)
    }

    @Test
    fun `a later assignment uses the declared type of the variable`() {
        val interpreter = run("let x: boolean = true;\nx = readInput(\"v: \");", "false")

        assertEquals(false, interpreter.variables["x"])
    }

    @Test
    fun `an assignment that cannot be read as the declared type fails`() {
        assertThrows<InterpreterException> {
            run("let n: number = 1;\nn = readInput(\"v: \");", "abc")
        }
    }

    @Test
    fun `a variable declared without a value still remembers its type`() {
        val interpreter = run("let n: number;\nn = readInput(\"v: \");", "10")

        assertEquals(10, interpreter.variables["n"])
    }

    @Test
    fun `readInput inside println is a string`() {
        // La consigna pide que sea string; el parser todavía no arma esta llamada, así que
        // el nodo se construye a mano.
        val position = TokenPosition(0, 0)
        val call =
            FunctionNode(
                TokenType.FUNCTION,
                "readInput",
                LiteralNode("v: ", TokenType.STRINGLITERAL, position),
                position,
            )
        val interpreter = Interpreter.forVersion("1.1", printer, readerOf("5"))

        interpreter.execute(PrintNode(call, position))

        assertEquals(listOf("v: ", "5"), outputs)
    }

    @Test
    fun `readEnv is read as the declared type too`() {
        assumeTrue(System.getenv("PATH") != null, "El test necesita una variable de ambiente conocida")

        val exception =
            assertThrows<InterpreterException> {
                run("let n: number = readEnv(\"PATH\");")
            }

        assertEquals(
            "readEnv devolvió \"${System.getenv("PATH")}\", que no puede interpretarse como number",
            exception.message,
        )
    }
}
