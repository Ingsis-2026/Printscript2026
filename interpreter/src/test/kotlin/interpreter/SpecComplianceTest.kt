package interpreter

import lexer.Lexer
import lexer.TokenMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import parser.Parser

/**
 * Casos tomados literalmente de la consigna (PrintScript 1.0), con su salida esperada.
 */
class SpecComplianceTest {
    private val reader =
        object : Reader {
            override fun input(message: String): String = ""
        }

    private fun runProgram(source: String): String {
        val outputs = mutableListOf<String>()
        val printer =
            object : Printer {
                override fun print(message: String) {
                    outputs.add(message)
                }
            }
        val tokens = Lexer(TokenMapper.forVersion("1.0")).execute(source)
        val astNodes = Parser.forVersion("1.0").execute(tokens)
        val interpreter = Interpreter.forVersion("1.0", printer, reader)
        astNodes.forEach { interpreter.execute(it) }
        return outputs.joinToString("\n")
    }

    @Test
    fun `ejemplo 1 - concatenacion de strings`() {
        val source =
            """
            let name: string = "Joe";
            let lastName: string = "Doe";
            println(name + " " + lastName);
            """.trimIndent()
        assertEquals("Joe Doe", runProgram(source))
    }

    @Test
    fun `ejemplo 2 - division y concatenacion string mas number`() {
        val source =
            """
            let a: number = 12;
            let b: number = 4;
            let c: number = a / b;
            println("Result: " + c);
            """.trimIndent()
        assertEquals("Result: 3", runProgram(source))
    }

    @Test
    fun `ejemplo 3 - reasignacion de una variable ya declarada`() {
        val source =
            """
            let a: number = 12;
            let b: number = 4;
            a = a / b;
            println("Result: " + a);
            """.trimIndent()
        assertEquals("Result: 3", runProgram(source))
    }

    // "El tipo number incluye enteros y decimales."

    @Test
    fun `number admite decimales en una declaracion`() {
        assertEquals("2.5", runProgram("let x: number = 2.5;\nprintln(x);"))
    }

    @Test
    fun `un number entero puede reasignarse a un decimal`() {
        val source =
            """
            let x: number = 5;
            x = 2.5;
            println(x);
            """.trimIndent()
        assertEquals("2.5", runProgram(source))
    }

    // "si la expresión incluye ambos tipos el resultado es string"

    @Test
    fun `concatenacion de string con number decimal`() {
        val source =
            """
            let c: number = 2.5;
            println("Result: " + c);
            """.trimIndent()
        assertEquals("Result: 2.5", runProgram(source))
    }

    // "Concatenación de variables y literales de tipo string."

    @Test
    fun `concatenacion de dos variables string sin literales`() {
        val source =
            """
            let a: string = "Joe";
            let b: string = "Doe";
            let c: string = a + b;
            println(c);
            """.trimIndent()
        assertEquals("JoeDoe", runProgram(source))
    }

    @Test
    fun `string declarado con comillas simples`() {
        assertEquals("hola", runProgram("let x: string = 'hola';\nprintln(x);"))
    }
}
