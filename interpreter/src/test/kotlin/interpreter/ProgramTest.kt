package interpreter

import lexer.Lexer
import lexer.TokenMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import parser.Parser
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.util.Scanner

class ProgramTest {
    private val printer: Printer =
        object : Printer {
            override fun print(message: String) {
                println(message)
            }
        }
    private val reader: Reader =
        object : Reader {
            override fun input(message: String): String {
                val scanner = Scanner(System.`in`)
                return scanner.nextLine()
            }
        }

    @Test
    fun `test lexer, parser, and interpreter`() {
        val input =
            """
            let x : number = 42;
            let y : number = 10;
            println(x + y);
            """.trimIndent()

        // Step 1: Lexer
        val tokenMapper = TokenMapper("1.0")
        val lexer = Lexer(tokenMapper)
        val tokens = lexer.execute(input)

        // Step 2: parser.Parser
        val parser = Parser()
        val astNodes = parser.execute(tokens) // Filter out null tokens if any

        // Step 3: interpreter.Interpreter
        val interpreter = Interpreter(printer, reader)

        // Redirect output stream to capture print statements
        val outputStream = ByteArrayOutputStream()
        System.setOut(PrintStream(outputStream))

        // Evaluate each AST node
        for (node in astNodes) {
            interpreter.execute(node)
        }

        // Check the output
        assertEquals("52", outputStream.toString().trim())
    }
}