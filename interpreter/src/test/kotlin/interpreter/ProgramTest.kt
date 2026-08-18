package interpreter

import lexer.Lexer
import lexer.TokenMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
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

    private fun runProgram(input: String, version: String = "1.0"): String {
        val outputs = mutableListOf<String>()
        val testPrinter = object : Printer {
            override fun print(message: String) {
                outputs.add(message)
            }
        }
        val tokenMapper = TokenMapper.forVersion(version)
        val lexer = Lexer(tokenMapper)
        val tokens = lexer.execute(input)

        val parser = Parser.forVersion(version)
        val astNodes = parser.execute(tokens)

        val interpreter = Interpreter.forVersion(version, testPrinter, reader)
        for (node in astNodes) {
            interpreter.execute(node)
        }

        return outputs.joinToString("\n")
    }

    @Test
    fun `test lexer, parser, and interpreter with arithmetic`() {
        val input =
            """
            let x : number = 42;
            let y : number = 10;
            println(x + y);
            """.trimIndent()

        val output = runProgram(input, "1.0")
        assertEquals("52", output)
    }

    @Test
    fun `test string concatenation e2e`() {
        val input =
            """
            let greeting : string = "Hello, ";
            let name : string = "World!";
            println(greeting + name);
            """.trimIndent()

        val output = runProgram(input, "1.0")
        assertEquals("Hello, World!", output)
    }

    @Test
    fun `test string and number concatenation e2e`() {
        val input =
            """
            let score : number = 100;
            let message : string = "Score: " + score;
            println(message);
            """.trimIndent()

        val output = runProgram(input, "1.0")
        assertEquals("Score: 100", output)
    }

    @Test
    fun `test variable reassignment e2e`() {
        val input =
            """
            let counter : number = 1;
            counter = counter + 5;
            counter = counter * 2;
            println(counter);
            """.trimIndent()

        val output = runProgram(input, "1.0")
        assertEquals("12", output)
    }

    @Test
    fun `test conditional execution if branch in version 1_1`() {
        val input =
            """
            if (true) {
                println("Executed True Branch");
            } else {
                println("Executed False Branch");
            }
            """.trimIndent()

        val output = runProgram(input, "1.1")
        assertEquals("Executed True Branch", output)
    }

    @Test
    fun `test conditional execution else branch in version 1_1`() {
        val input =
            """
            if (false) {
                println("Executed True Branch");
            } else {
                println("Executed Else Branch");
            }
            """.trimIndent()

        val output = runProgram(input, "1.1")
        assertEquals("Executed Else Branch", output)
    }

    @Test
    fun `test const reassignation throws exception in version 1_1`() {
        val input =
            """
            const pi : number = 3;
            pi = 4;
            """.trimIndent()

        assertThrows<RuntimeException> {
            runProgram(input, "1.1")
        }
    }
}