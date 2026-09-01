package interpreter

import ast.ASTNode
import ast.AssignationNode
import ast.BinaryNode
import ast.BlockNode
import ast.ConditionalNode
import ast.DeclarationNode
import ast.FunctionNode
import ast.LiteralNode
import ast.NilNode
import ast.PrintNode
import interpreter.evaluators.NodeEvaluator
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import token.Token
import token.TokenPosition
import token.TokenType
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.util.Scanner
import kotlin.test.assertNull

class InterpreterTests {
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
    private val position = TokenPosition(1, 1)

    @Test
    fun `test number literal evaluation`() {
        val node = LiteralNode("42", TokenType.NUMBERLITERAL, position)
        val interpreter = Interpreter(printer, reader)
        val result = interpreter.execute(node)
        assertEquals(42, result)
    }

    @Test
    fun `test string literal evaluation`() {
        val node = LiteralNode("Hello, world!", TokenType.STRINGLITERAL, position)
        val interpreter = Interpreter(printer, reader)
        val result = interpreter.execute(node)
        assertEquals("Hello, world!", result)
    }

    @Test
    fun testAssignment() {
        val expression = LiteralNode("42", TokenType.NUMBERLITERAL, position)
        val node = AssignationNode("x", expression, TokenType.ASSIGNATION, position)
        val interpreter = Interpreter(printer, reader)
        interpreter.execute(node)
        assertEquals(42, interpreter.variables["x"])
    }

    @Test
    fun testDeclaration() {
        val expression = LiteralNode("42", TokenType.NUMBERLITERAL, position)
        val node = DeclarationNode(TokenType.KEYWORD, "let", "x", TokenType.DATA_TYPE, "number", expression, position)
        val interpreter = Interpreter(printer, reader)
        interpreter.execute(node)
        assertEquals(42, interpreter.variables["x"])
    }

    @Test
    fun `test print concatenation hello world 1`() {
        val left = LiteralNode("HelloWorld", TokenType.STRINGLITERAL, position)
        val right = LiteralNode("1", TokenType.NUMBERLITERAL, position)
        val operatorToken = Token(TokenType.OPERATOR, "+", position, position) // Create a token for "+"
        val node = BinaryNode(left, right, operatorToken, position)
        val printNode = PrintNode(node, position)
        val interpreter = Interpreter(printer, reader)

        // Redirect output stream to capture print statements
        val outputStream = ByteArrayOutputStream()
        System.setOut(PrintStream(outputStream))

        interpreter.execute(printNode)
        assertEquals("HelloWorld1", outputStream.toString().trim())
    }

    @Test
    fun `test boolean literal evaluation`() {
        val trueNode = LiteralNode("true", TokenType.BOOLEANLITERAL, position)
        val falseNode = LiteralNode("false", TokenType.BOOLEANLITERAL, position)
        val interpreter = Interpreter(printer, reader)
        assertEquals(true, interpreter.execute(trueNode))
        assertEquals(false, interpreter.execute(falseNode))
    }

    @Test
    fun `test addition of integers`() {
        val left = LiteralNode("10", TokenType.NUMBERLITERAL, position)
        val right = LiteralNode("5", TokenType.NUMBERLITERAL, position)
        val operatorToken = Token(TokenType.OPERATOR, "+", position, position) // Create a token for "+"
        val node = BinaryNode(left, right, operatorToken, position)
        val interpreter = Interpreter(printer, reader)
        val result = interpreter.execute(node)
        assertEquals(15, result)
    }

    @Test
    fun `test addition of integer and text`() {
        val left = LiteralNode("10", TokenType.NUMBERLITERAL, position)
        val right = LiteralNode("5", TokenType.STRINGLITERAL, position)
        val operatorToken = Token(TokenType.OPERATOR, "+", position, position) // Create a token for "+"
        val node = BinaryNode(left, right, operatorToken, position)
        val interpreter = Interpreter(printer, reader)

        val result = interpreter.execute(node)
        assertEquals("105", result)
    }

    @Test
    fun `test addition of integer and text2`() {
        val left = LiteralNode("10", TokenType.STRINGLITERAL, position)
        val right = LiteralNode("5", TokenType.NUMBERLITERAL, position)
        val operatorToken = Token(TokenType.OPERATOR, "+", position, position) // Create a token for "+"
        val node = BinaryNode(left, right, operatorToken, position)
        val interpreter = Interpreter(printer, reader)

        val result = interpreter.execute(node)
        assertEquals("105", result)
    }

    @Test
    fun `test string concatenation`() {
        val left = LiteralNode("Hello", TokenType.STRINGLITERAL, position)
        val right = LiteralNode("World", TokenType.STRINGLITERAL, position)
        val operatorToken = Token(TokenType.OPERATOR, "+", position, position) // Create a token for "+"
        val node = BinaryNode(left, right, operatorToken, position)
        val interpreter = Interpreter(printer, reader)
        val result = interpreter.execute(node)
        assertEquals("HelloWorld", result)
    }

    @Test
    fun `test subtraction of integers`() {
        val left = LiteralNode("15", TokenType.NUMBERLITERAL, position)
        val right = LiteralNode("5", TokenType.NUMBERLITERAL, position)
        val operatorToken = Token(TokenType.OPERATOR, "-", position, position) // Create a token for "-"
        val node = BinaryNode(left, right, operatorToken, position)
        val interpreter = Interpreter(printer, reader)
        val result = interpreter.execute(node)
        assertEquals(10, result)
    }

    @Test
    fun `test multiplication of integers`() {
        val left = LiteralNode("3", TokenType.NUMBERLITERAL, position)
        val right = LiteralNode("4", TokenType.NUMBERLITERAL, position)
        val operatorToken = Token(TokenType.OPERATOR, "*", position, position) // Create a token for "*"
        val node = BinaryNode(left, right, operatorToken, position)
        val interpreter = Interpreter(printer, reader)
        val result = interpreter.execute(node)
        assertEquals(12, result)
    }

    @Test
    fun `test division of integers`() {
        val left = LiteralNode("20", TokenType.NUMBERLITERAL, position)
        val right = LiteralNode("4", TokenType.NUMBERLITERAL, position)
        val operatorToken = Token(TokenType.OPERATOR, "/", position, position) // Create a token for "/"
        val node = BinaryNode(left, right, operatorToken, position)
        val interpreter = Interpreter(printer, reader)
        val result = interpreter.execute(node)
        assertEquals(5, result)
    }

    @Test
    fun `test division by zero`() {
        val left = LiteralNode("20", TokenType.NUMBERLITERAL, position)
        val right = LiteralNode("0", TokenType.NUMBERLITERAL, position)
        val operatorToken = Token(TokenType.OPERATOR, "/", position, position) // Create a token for "/"
        val node = BinaryNode(left, right, operatorToken, position)
        val interpreter = Interpreter(printer, reader)
        assertThrows(RuntimeException::class.java) {
            interpreter.execute(node)
        }
    }

    @Test
    fun `test variable assignment`() {
        val expression = LiteralNode("42", TokenType.NUMBERLITERAL, position)
        val node = AssignationNode("x", expression, TokenType.ASSIGNATION, position)
        val interpreter = Interpreter(printer, reader)
        interpreter.execute(node)
        assertEquals(42, interpreter.variables["x"])
    }

    @Test
    fun `test print node`() {
        val expression = LiteralNode("Hello, world!", TokenType.STRINGLITERAL, position)
        val node = PrintNode(expression, position)
        val interpreter = Interpreter(printer, reader)

        // Redirect output stream to capture print statements
        val outputStream = ByteArrayOutputStream()
        System.setOut(PrintStream(outputStream))

        interpreter.execute(node)
        assertEquals("Hello, world!", outputStream.toString().trim())
    }

    @Test
    fun `test block execution`() {
        val expr1 = LiteralNode("Hello", TokenType.STRINGLITERAL, position)
        val expr2 = LiteralNode("World", TokenType.STRINGLITERAL, position)
        val node = BlockNode(listOf(expr1, expr2), position)
        val interpreter = Interpreter(printer, reader)
        assertEquals("World", interpreter.execute(node))
    }

    @Test
    fun `test conditional node true`() {
        val condition = LiteralNode("true", TokenType.BOOLEANLITERAL, position)
        val thenBlock = PrintNode(LiteralNode("Condition is true", TokenType.STRINGLITERAL, position), position)
        val elseBlock = PrintNode(LiteralNode("Condition is false", TokenType.STRINGLITERAL, position), position)
        val node = ConditionalNode(condition, thenBlock, elseBlock, position)
        val interpreter = Interpreter(printer, reader)

        // Redirect output stream to capture print statements
        val outputStream = ByteArrayOutputStream()
        System.setOut(PrintStream(outputStream))

        interpreter.execute(node)
        assertEquals("Condition is true", outputStream.toString().trim())
    }

    @Test
    fun `test conditional node false`() {
        val condition = LiteralNode("false", TokenType.BOOLEANLITERAL, position)
        val thenBlock = PrintNode(LiteralNode("Condition is true", TokenType.STRINGLITERAL, position), position)
        val elseBlock = PrintNode(LiteralNode("Condition is false", TokenType.STRINGLITERAL, position), position)
        val node = ConditionalNode(condition, thenBlock, elseBlock, position)
        val interpreter = Interpreter(printer, reader)

        // Redirect output stream to capture print statements
        val outputStream = ByteArrayOutputStream()
        System.setOut(PrintStream(outputStream))

        interpreter.execute(node)
        assertEquals("Condition is false", outputStream.toString().trim())
    }

    @Test
    fun `test undefined variable exception`() {
        val node = LiteralNode("undefinedVariable", TokenType.IDENTIFIER, position)
        val interpreter = Interpreter(printer, reader)

        assertThrows(RuntimeException::class.java) {
            interpreter.execute(node)
        }
    }

    @Test
    fun `test greater than operator`() {
        val left = LiteralNode("10", TokenType.NUMBERLITERAL, position)
        val right = LiteralNode("5", TokenType.NUMBERLITERAL, position)
        val operatorToken = Token(TokenType.OPERATOR, ">", position, position) // Create a token for ">"
        val node = BinaryNode(left, right, operatorToken, position)
        val interpreter = Interpreter(printer, reader)

        val result = interpreter.execute(node)

        // Assuming the result is a Boolean, we expect "10 > 5" to be true
        assertEquals(true, result)
    }

    @Test
    fun `test unsupported operator exception`() {
        val left = LiteralNode("10", TokenType.NUMBERLITERAL, position)
        val right = LiteralNode("5", TokenType.NUMBERLITERAL, position)
        val operatorToken = Token(TokenType.OPERATOR, "^", position, position) // Use an unsupported operator
        val node = BinaryNode(left, right, operatorToken, position)
        val interpreter = Interpreter(printer, reader)

        assertThrows(RuntimeException::class.java) {
            interpreter.execute(node)
        }
    }

    @Test
    fun `test less than operator`() {
        val left = LiteralNode("5", TokenType.NUMBERLITERAL, position)
        val right = LiteralNode("10", TokenType.NUMBERLITERAL, position)
        val operatorToken = Token(TokenType.OPERATOR, "<", position, position) // Create a token for "<"
        val node = BinaryNode(left, right, operatorToken, position)
        val interpreter = Interpreter(printer, reader)

        val result = interpreter.execute(node)

        // Assuming the result is a Boolean, we expect "5 < 10" to be true
        assertEquals(true, result)
    }

    @Test
    fun `test supported operator exception`() {
        val left = LiteralNode("10", TokenType.NUMBERLITERAL, position)
        val right = LiteralNode("5", TokenType.NUMBERLITERAL, position)
        val operatorToken = Token(TokenType.OPERATOR, "+", position, position) // Use a supported operator
        val node = BinaryNode(left, right, operatorToken, position)
        val interpreter = Interpreter(printer, reader)

        assertDoesNotThrow {
            interpreter.execute(node)
        }
    }

    @Test
    fun `test greater than exception`() {
        val left = LiteralNode("10", TokenType.NUMBERLITERAL, position)
        val right = LiteralNode("true", TokenType.BOOLEAN, position)
        val operatorToken = Token(TokenType.OPERATOR, ">", position, position) // Create a token for ">"
        val node = BinaryNode(left, right, operatorToken, position)
        val interpreter = Interpreter(printer, reader)

        assertThrows(RuntimeException::class.java) {
            interpreter.execute(node)
        }
    }

    @Test
    fun `test less than exception`() {
        val left = LiteralNode("10", TokenType.NUMBERLITERAL, position)
        val right = LiteralNode("true", TokenType.BOOLEAN, position)
        val operatorToken = Token(TokenType.OPERATOR, "<", position, position) // Create a token for "<"
        val node = BinaryNode(left, right, operatorToken, position)
        val interpreter = Interpreter(printer, reader)

        assertThrows(RuntimeException::class.java) {
            interpreter.execute(node)
        }
    }

    @Test
    fun `test function node`() {
        val expression = LiteralNode("Hello, world!", TokenType.STRINGLITERAL, position)
        val node = FunctionNode(TokenType.FUNCTION, "println", expression, position)
        val interpreter = Interpreter(printer, reader)

        // Redirect output stream to capture print statements
        val outputStream = ByteArrayOutputStream()
        System.setOut(PrintStream(outputStream))

        interpreter.execute(node)
        assertEquals("Hello, world!", outputStream.toString().trim())
    }

    @Test
    fun `test declaration node`() {
        val expression = LiteralNode("42", TokenType.NUMBERLITERAL, position)
        val node = DeclarationNode(TokenType.KEYWORD, "let", "x", TokenType.DATA_TYPE, "number", expression, position)
        val interpreter = Interpreter(printer, reader)
        interpreter.execute(node)
        assertEquals(42, interpreter.variables["x"])
    }

    @Test
    fun `test programmatic script execution`() {
        val interpreter = Interpreter(printer, reader)

        // Asignación
        val assignNode =
            AssignationNode("x", LiteralNode("10", TokenType.NUMBERLITERAL, position), TokenType.ASSIGNATION, position)
        interpreter.execute(assignNode)

        // Suma
        val additionNode =
            BinaryNode(
                LiteralNode("x", TokenType.IDENTIFIER, position),
                LiteralNode("5", TokenType.NUMBERLITERAL, position),
                Token(TokenType.OPERATOR, "+", position, position),
                position,
            )
        val result = interpreter.execute(additionNode)

        // Verificar
        assertEquals(15, result)
        assertEquals(10, interpreter.variables["x"]) // Verificar que 'x' sigue siendo 10
    }

    @Test
    fun `test programmatic block execution`() {
        val interpreter = Interpreter(printer, reader)

        val expr1 =
            AssignationNode("a", LiteralNode("5", TokenType.NUMBERLITERAL, position), TokenType.ASSIGNATION, position)
        val expr2 =
            AssignationNode("b", LiteralNode("10", TokenType.NUMBERLITERAL, position), TokenType.ASSIGNATION, position)
        val sumNode =
            BinaryNode(
                LiteralNode("a", TokenType.IDENTIFIER, position),
                LiteralNode("b", TokenType.IDENTIFIER, position),
                Token(TokenType.OPERATOR, "+", position, position),
                position,
            )

        val block = BlockNode(listOf(expr1, expr2, sumNode), position)
        val result = interpreter.execute(block)

        assertEquals(15, result)
    }

    @Test
    fun `test programmatic function execution`() {
        val interpreter = Interpreter(printer, reader)

        // Definir una función ficticia
        val functionBody =
            BinaryNode(
                LiteralNode("x", TokenType.IDENTIFIER, position),
                LiteralNode("5", TokenType.NUMBERLITERAL, position),
                Token(TokenType.OPERATOR, "+", position, position),
                position,
            )
        val functionNode = FunctionNode(TokenType.FUNCTION, "println", functionBody, position)

        // Asignar un valor a x
        interpreter.variables["x"] = 10

        // Ejecutar la función
        val result = interpreter.execute(functionNode)

        // Verificar
        assertEquals(15, result) // 10 + 5
    }
    /*
    @Test
    fun `test readEnv function`() {
        // Configurar el entorno para que devuelva un valor específico
        val envVariable = "BEST_FOOTBALL_CLUB"
        System.setProperty(envVariable, "Barcelona") // Configura la variable de entorno para la prueba

        val interpreter = Interpreter(printer)

        // Crear el nodo de función readEnv
        val readEnvNode = FunctionNode(TokenType.FUNCTION, LiteralNode(envVariable, TokenType.STRINGLITERAL, position), position)

        // Ejecutar la función readEnv
        val result = interpreter.execute(readEnvNode)

        // Verificar que el resultado sea el valor de la variable de entorno
        assertEquals("Barcelona", result)

        // Limpiar la variable de entorno después de la prueba
        System.clearProperty(envVariable)
    }

     */

    @Test
    fun `test programmatic script with multiple statements`() {
        val interpreter = Interpreter(printer, reader)

        // Declarar varias variables
        interpreter.execute(
            AssignationNode(
                "x",
                LiteralNode("10", TokenType.NUMBERLITERAL, position),
                TokenType.ASSIGNATION,
                position,
            ),
        )
        interpreter.execute(
            AssignationNode(
                "y",
                LiteralNode("20", TokenType.NUMBERLITERAL, position),
                TokenType.ASSIGNATION,
                position,
            ),
        )

        // Sumar y asignar a otra variable
        val sumNode =
            BinaryNode(
                LiteralNode("x", TokenType.IDENTIFIER, position),
                LiteralNode("y", TokenType.IDENTIFIER, position),
                Token(TokenType.OPERATOR, "+", position, position),
                position,
            )
        interpreter.execute(AssignationNode("z", sumNode, TokenType.ASSIGNATION, position))

        // Verificar que las variables están correctamente asignadas
        assertEquals(10, interpreter.variables["x"])
        assertEquals(20, interpreter.variables["y"])
        assertEquals(30, interpreter.variables["z"])
    }

    /*
    @Test
    fun `test invalid expression for type`() {
        val interpreter = Interpreter()

        // Crear un nodo de asignación con tipo 'number' pero con una expresión no válida
        val invalidExpressionNode =
            AssignationNode(
                id = "pi",
                expression = LiteralNode("3.14159", TokenType.NUMBERLITERAL, position),
                valType = TokenType.DATA_TYPE,
                position = position,
            )
        assertEquals(3.14159, interpreter.execute(invalidExpressionNode))
    }

    @Test
    fun `test invalid arithmetic operation with strings`() {
        val left = LiteralNode("hello", TokenType.STRINGLITERAL, position)
        val right = LiteralNode("5", TokenType.NUMBERLITERAL, position)
        val operatorToken = Token(TokenType.OPERATOR, "+", position, position)
        val node = BinaryNode(left, right, operatorToken, position)
        val interpreter = Interpreter()
        assertThrows(RuntimeException::class.java) {
            interpreter.execute(node)
        }
    }


     */
    @Test
    fun `test concatenation of StringNumber and Number`() {
        val left = LiteralNode("2", TokenType.STRINGLITERAL, position)
        val right = LiteralNode("5", TokenType.NUMBERLITERAL, position)
        val operatorToken = Token(TokenType.OPERATOR, "+", position, position) // Create a token for "+"
        val node = BinaryNode(left, right, operatorToken, position)
        val interpreter = Interpreter(printer, reader)
        assertEquals("25", interpreter.execute(node))
    }

    @Test
    fun `test handleReadEnv with undefined environment variable`() {
        val interpreter = Interpreter(printer, reader)
        val envVariable = "BEST_FOOTBALL_CLUB"
        val node = FunctionNode(TokenType.FUNCTION, "readEnv", LiteralNode(envVariable, TokenType.STRINGLITERAL, position), position)
        assertThrows(RuntimeException::class.java) {
            interpreter.execute(node)
        }
    }

    @Test
    fun `test convertInput with boolean true`() {
        val interpreter = Interpreter(printer, reader)
        val result = interpreter.convertInput("true")
        assertEquals(true, result)
    }

    @Test
    fun `test convertInput with integer`() {
        val interpreter = Interpreter(printer, reader)
        val result = interpreter.convertInput("123")
        assertEquals(123, result)
    }

    @Test
    fun `test convertInput with double`() {
        val interpreter = Interpreter(printer, reader)
        val result = interpreter.convertInput("123.45")
        assertEquals(123.45, result)
    }

    @Test
    fun `test convertInput with string`() {
        val interpreter = Interpreter(printer, reader)
        val result = interpreter.convertInput("hello")
        assertEquals("hello", result)
    }

    @Test
    fun `test addition of two integers`() {
        val left = LiteralNode("10", TokenType.NUMBERLITERAL, position)
        val right = LiteralNode("5", TokenType.NUMBERLITERAL, position)
        val operatorToken = Token(TokenType.OPERATOR, "+", position, position)
        val node = BinaryNode(left, right, operatorToken, position)
        val interpreter = Interpreter(printer, reader)

        val result = interpreter.execute(node)
        assertEquals(15, result)
    }

    @Test
    fun `test addition of two strings`() {
        val left = LiteralNode("Hello", TokenType.STRINGLITERAL, position)
        val right = LiteralNode(" World", TokenType.STRINGLITERAL, position)
        val operatorToken = Token(TokenType.OPERATOR, "+", position, position)
        val node = BinaryNode(left, right, operatorToken, position)
        val interpreter = Interpreter(printer, reader)

        val result = interpreter.execute(node)
        assertEquals("Hello World", result)
    }

    @Test
    fun `test addition of integer and string`() {
        val left = LiteralNode("10", TokenType.NUMBERLITERAL, position)
        val right = LiteralNode(" apples", TokenType.STRINGLITERAL, position)
        val operatorToken = Token(TokenType.OPERATOR, "+", position, position)
        val node = BinaryNode(left, right, operatorToken, position)
        val interpreter = Interpreter(printer, reader)

        val result = interpreter.execute(node)
        assertEquals("10 apples", result)
    }

    @Test
    fun `test addition of string and integer`() {
        val left = LiteralNode("Count: ", TokenType.STRINGLITERAL, position)
        val right = LiteralNode("5", TokenType.NUMBERLITERAL, position)
        val operatorToken = Token(TokenType.OPERATOR, "+", position, position)
        val node = BinaryNode(left, right, operatorToken, position)
        val interpreter = Interpreter(printer, reader)

        val result = interpreter.execute(node)
        assertEquals("Count: 5", result)
    }

    @Test
    fun `test addition of two doubles`() {
        val left = LiteralNode("3.14", TokenType.NUMBERLITERAL, position)
        val right = LiteralNode("2.71", TokenType.NUMBERLITERAL, position)
        val operatorToken = Token(TokenType.OPERATOR, "+", position, position)
        val node = BinaryNode(left, right, operatorToken, position)
        val interpreter = Interpreter(printer, reader)

        val result = interpreter.execute(node)
        assertEquals(5.85, result)
    }

    @Test
    fun `test subtraction of two integers`() {
        val left = LiteralNode("10", TokenType.NUMBERLITERAL, position)
        val right = LiteralNode("5", TokenType.NUMBERLITERAL, position)
        val operatorToken = Token(TokenType.OPERATOR, "-", position, position)
        val node = BinaryNode(left, right, operatorToken, position)
        val interpreter = Interpreter(printer, reader)

        val result = interpreter.execute(node)
        assertEquals(5, result)
    }

    @Test
    fun `test subtraction of two floats`() {
        val left = LiteralNode("10.5", TokenType.NUMBERLITERAL, position)
        val right = LiteralNode("5.2", TokenType.NUMBERLITERAL, position)
        val operatorToken = Token(TokenType.OPERATOR, "-", position, position)
        val node = BinaryNode(left, right, operatorToken, position)
        val interpreter = Interpreter(printer, reader)

        val result = interpreter.execute(node)
        assertEquals(5.3, result)
    }

    @Test
    fun `test subtraction of int and float`() {
        val left = LiteralNode("10", TokenType.NUMBERLITERAL, position)
        val right = LiteralNode("2.5", TokenType.NUMBERLITERAL, position)
        val operatorToken = Token(TokenType.OPERATOR, "-", position, position)
        val node = BinaryNode(left, right, operatorToken, position)
        val interpreter = Interpreter(printer, reader)

        val result = interpreter.execute(node)
        assertEquals(7.5, result)
    }

    @Test
    fun `test subtraction of float and int`() {
        val left = LiteralNode("10.5", TokenType.NUMBERLITERAL, position)
        val right = LiteralNode("5", TokenType.NUMBERLITERAL, position)
        val operatorToken = Token(TokenType.OPERATOR, "-", position, position)
        val node = BinaryNode(left, right, operatorToken, position)
        val interpreter = Interpreter(printer, reader)

        val result = interpreter.execute(node)
        assertEquals(5.5, result)
    }

    @Test
    fun `test subtraction of two doubles`() {
        val left = LiteralNode("10.75", TokenType.NUMBERLITERAL, position)
        val right = LiteralNode("3.25", TokenType.NUMBERLITERAL, position)
        val operatorToken = Token(TokenType.OPERATOR, "-", position, position)
        val node = BinaryNode(left, right, operatorToken, position)
        val interpreter = Interpreter(printer, reader)

        val result = interpreter.execute(node)
        assertEquals(7.5, result)
    }

    @Test
    fun `test multiplication of two integers`() {
        val left = LiteralNode("6", TokenType.NUMBERLITERAL, position)
        val right = LiteralNode("7", TokenType.NUMBERLITERAL, position)
        val operatorToken = Token(TokenType.OPERATOR, "*", position, position)
        val node = BinaryNode(left, right, operatorToken, position)
        val interpreter = Interpreter(printer, reader)

        val result = interpreter.execute(node)
        assertEquals(42, result)
    }

    @Test
    fun `test multiplication of two floats`() {
        val left = LiteralNode("2.5", TokenType.NUMBERLITERAL, position)
        val right = LiteralNode("3.5", TokenType.NUMBERLITERAL, position)
        val operatorToken = Token(TokenType.OPERATOR, "*", position, position)
        val node = BinaryNode(left, right, operatorToken, position)
        val interpreter = Interpreter(printer, reader)

        val result = interpreter.execute(node)
        assertEquals(8.75, result)
    }

    @Test
    fun `test multiplication of integer and float`() {
        val left = LiteralNode("4", TokenType.NUMBERLITERAL, position)
        val right = LiteralNode("2.5", TokenType.NUMBERLITERAL, position)
        val operatorToken = Token(TokenType.OPERATOR, "*", position, position)
        val node = BinaryNode(left, right, operatorToken, position)
        val interpreter = Interpreter(printer, reader)

        val result = interpreter.execute(node)
        assertEquals(10.0, result)
    }

    @Test
    fun `test multiplication of float and integer`() {
        val left = LiteralNode("2.5", TokenType.NUMBERLITERAL, position)
        val right = LiteralNode("4", TokenType.NUMBERLITERAL, position)
        val operatorToken = Token(TokenType.OPERATOR, "*", position, position)
        val node = BinaryNode(left, right, operatorToken, position)
        val interpreter = Interpreter(printer, reader)

        val result = interpreter.execute(node)
        assertEquals(10.0, result)
    }

    @Test
    fun `test multiplication of two doubles`() {
        val left = LiteralNode("3.5", TokenType.NUMBERLITERAL, position)
        val right = LiteralNode("2.0", TokenType.NUMBERLITERAL, position)
        val operatorToken = Token(TokenType.OPERATOR, "*", position, position)
        val node = BinaryNode(left, right, operatorToken, position)
        val interpreter = Interpreter(printer, reader)

        val result = interpreter.execute(node)
        assertEquals(7.0, result)
    }

    @Test
    fun `test multiplication of integer and double`() {
        val left = LiteralNode("4", TokenType.NUMBERLITERAL, position)
        val right = LiteralNode("2.5", TokenType.NUMBERLITERAL, position)
        val operatorToken = Token(TokenType.OPERATOR, "*", position, position)
        val node = BinaryNode(left, right, operatorToken, position)
        val interpreter = Interpreter(printer, reader)

        val result = interpreter.execute(node)
        assertEquals(10.0, result)
    }

    @Test
    fun `test multiplication of double and integer`() {
        val left = LiteralNode("2.5", TokenType.NUMBERLITERAL, position)
        val right = LiteralNode("4", TokenType.NUMBERLITERAL, position)
        val operatorToken = Token(TokenType.OPERATOR, "*", position, position)
        val node = BinaryNode(left, right, operatorToken, position)
        val interpreter = Interpreter(printer, reader)

        val result = interpreter.execute(node)
        assertEquals(10.0, result)
    }

    @Test
    fun `test multiplication of string with integer throws exception`() {
        val left = LiteralNode("Hello", TokenType.STRINGLITERAL, position)
        val right = LiteralNode("4", TokenType.NUMBERLITERAL, position)
        val operatorToken = Token(TokenType.OPERATOR, "*", position, position)
        val node = BinaryNode(left, right, operatorToken, position)
        val interpreter = Interpreter(printer, reader)

        assertThrows(RuntimeException::class.java) {
            interpreter.execute(node)
        }
    }

    @Test
    fun `test multiplication of integer with string throws exception`() {
        val left = LiteralNode("4", TokenType.NUMBERLITERAL, position)
        val right = LiteralNode("Hello", TokenType.STRINGLITERAL, position)
        val operatorToken = Token(TokenType.OPERATOR, "*", position, position)
        val node = BinaryNode(left, right, operatorToken, position)
        val interpreter = Interpreter(printer, reader)

        assertThrows(RuntimeException::class.java) {
            interpreter.execute(node)
        }
    }

    @Test
    fun `test division of two integers`() {
        val left = LiteralNode("10", TokenType.NUMBERLITERAL, position)
        val right = LiteralNode("2", TokenType.NUMBERLITERAL, position)
        val operatorToken = Token(TokenType.OPERATOR, "/", position, position)
        val node = BinaryNode(left, right, operatorToken, position)
        val interpreter = Interpreter(printer, reader)

        val result = interpreter.execute(node)
        assertEquals(5, result)
    }

    @Test
    fun `test division of two floats`() {
        val left = LiteralNode("7.5", TokenType.NUMBERLITERAL, position)
        val right = LiteralNode("2.5", TokenType.NUMBERLITERAL, position)
        val operatorToken = Token(TokenType.OPERATOR, "/", position, position)
        val node = BinaryNode(left, right, operatorToken, position)
        val interpreter = Interpreter(printer, reader)

        val result = interpreter.execute(node)
        assertEquals(3.0, result)
    }

    @Test
    fun `test division of integer by float`() {
        val left = LiteralNode("10", TokenType.NUMBERLITERAL, position)
        val right = LiteralNode("4.0", TokenType.NUMBERLITERAL, position)
        val operatorToken = Token(TokenType.OPERATOR, "/", position, position)
        val node = BinaryNode(left, right, operatorToken, position)
        val interpreter = Interpreter(printer, reader)

        val result = interpreter.execute(node)
        assertEquals(2.5, result)
    }

    @Test
    fun `test division of float by integer`() {
        val left = LiteralNode("7.5", TokenType.NUMBERLITERAL, position)
        val right = LiteralNode("3", TokenType.NUMBERLITERAL, position)
        val operatorToken = Token(TokenType.OPERATOR, "/", position, position)
        val node = BinaryNode(left, right, operatorToken, position)
        val interpreter = Interpreter(printer, reader)

        val result = interpreter.execute(node)
        assertEquals(2.5, result)
    }

    @Test
    fun `test division of two doubles`() {
        val left = LiteralNode("9.0", TokenType.NUMBERLITERAL, position)
        val right = LiteralNode("3.0", TokenType.NUMBERLITERAL, position)
        val operatorToken = Token(TokenType.OPERATOR, "/", position, position)
        val node = BinaryNode(left, right, operatorToken, position)
        val interpreter = Interpreter(printer, reader)

        val result = interpreter.execute(node)
        assertEquals(3.0, result)
    }

    @Test
    fun `test division of integer by double`() {
        val left = LiteralNode("10", TokenType.NUMBERLITERAL, position)
        val right = LiteralNode("4.0", TokenType.NUMBERLITERAL, position)
        val operatorToken = Token(TokenType.OPERATOR, "/", position, position)
        val node = BinaryNode(left, right, operatorToken, position)
        val interpreter = Interpreter(printer, reader)

        val result = interpreter.execute(node)
        assertEquals(2.5, result)
    }

    @Test
    fun `test division of double by integer`() {
        val left = LiteralNode("10.0", TokenType.NUMBERLITERAL, position)
        val right = LiteralNode("2", TokenType.NUMBERLITERAL, position)
        val operatorToken = Token(TokenType.OPERATOR, "/", position, position)
        val node = BinaryNode(left, right, operatorToken, position)
        val interpreter = Interpreter(printer, reader)

        val result = interpreter.execute(node)
        assertEquals(5.0, result)
    }

    @Test
    fun `test division by zero throws exception`() {
        val left = LiteralNode("10", TokenType.NUMBERLITERAL, position)
        val right = LiteralNode("0", TokenType.NUMBERLITERAL, position)
        val operatorToken = Token(TokenType.OPERATOR, "/", position, position)
        val node = BinaryNode(left, right, operatorToken, position)
        val interpreter = Interpreter(printer, reader)

        assertThrows(RuntimeException::class.java) {
            interpreter.execute(node)
        }
    }

    @Test
    fun `test division of string with number throws exception`() {
        val left = LiteralNode("Hello", TokenType.STRINGLITERAL, position)
        val right = LiteralNode("5", TokenType.NUMBERLITERAL, position)
        val operatorToken = Token(TokenType.OPERATOR, "/", position, position)
        val node = BinaryNode(left, right, operatorToken, position)
        val interpreter = Interpreter(printer, reader)

        assertThrows(RuntimeException::class.java) {
            interpreter.execute(node)
        }
    }

    @Test
    fun `test division of number with string throws exception`() {
        val left = LiteralNode("5", TokenType.NUMBERLITERAL, position)
        val right = LiteralNode("Hello", TokenType.STRINGLITERAL, position)
        val operatorToken = Token(TokenType.OPERATOR, "/", position, position)
        val node = BinaryNode(left, right, operatorToken, position)
        val interpreter = Interpreter(printer, reader)

        assertThrows(RuntimeException::class.java) {
            interpreter.execute(node)
        }
    }

    @Test
    fun `test assignment of integer variable`() {
        val expression = LiteralNode("10", TokenType.NUMBERLITERAL, position)
        val node = AssignationNode("x", expression, TokenType.NUMBERLITERAL, position) // Ahora incluye valType
        val interpreter = Interpreter(printer, reader)

        val result = interpreter.execute(node)

        assertEquals(10, interpreter.variables["x"])
        assertEquals(10, result)
    }

    @Test
    fun `test assignment of string variable`() {
        val expression = LiteralNode("Hello", TokenType.STRINGLITERAL, position)
        val node = AssignationNode("greeting", expression, TokenType.STRINGLITERAL, position) // Ahora incluye valType
        val interpreter = Interpreter(printer, reader)

        val result = interpreter.execute(node)

        assertEquals("Hello", interpreter.variables["greeting"])
        assertEquals("Hello", result)
    }

    @Test
    fun `test reassignment of variable with same type`() {
        val interpreter = Interpreter(printer, reader)

        // Primera asignación
        val initialExpression = LiteralNode("42", TokenType.NUMBERLITERAL, position)
        val initialNode = AssignationNode("numberVar", initialExpression, TokenType.NUMBERLITERAL, position)
        interpreter.execute(initialNode)

        // Reasignación con el mismo tipo
        val newExpression = LiteralNode("100", TokenType.NUMBERLITERAL, position)
        val newNode = AssignationNode("numberVar", newExpression, TokenType.NUMBERLITERAL, position)
        val result = interpreter.execute(newNode)

        assertEquals(100, interpreter.variables["numberVar"])
        assertEquals(100, result)
    }

    @Test
    fun `test reassignment of variable with different type throws exception`() {
        val interpreter = Interpreter(printer, reader)

        // Asignación inicial de entero
        val initialExpression = LiteralNode("42", TokenType.NUMBERLITERAL, position)
        val initialNode = AssignationNode("varTest", initialExpression, TokenType.NUMBERLITERAL, position)
        interpreter.execute(initialNode)

        // Intento de reasignar con un string
        val newExpression = LiteralNode("New Value", TokenType.STRINGLITERAL, position)
        val newNode = AssignationNode("varTest", newExpression, TokenType.STRINGLITERAL, position)

        val exception =
            assertThrows(RuntimeException::class.java) {
                interpreter.execute(newNode)
            }
        assertEquals("Invalid expression for type numberliteral", exception.message)
    }

    @Test
    fun `test assignment to const variable throws exception`() {
        val interpreter = Interpreter(printer, reader)

        // Asignación inicial de una constante
        interpreter.variables["constVar"] = "FixedValue"
        interpreter.tiposDeVariables["constVar"] = "const"

        // Intento de reasignar una constante
        val newExpression = LiteralNode("New Value", TokenType.STRINGLITERAL, position)
        val newNode = AssignationNode("constVar", newExpression, TokenType.STRINGLITERAL, position)

        val exception =
            assertThrows(RuntimeException::class.java) {
                interpreter.execute(newNode)
            }
        assertEquals("No es posible reasignar una variable de tipo const", exception.message)
    }

    @Test
    fun `test variable assignment prints debug information`() {
        val expression = LiteralNode("30", TokenType.NUMBERLITERAL, position)
        val node = AssignationNode("debugVar", expression, TokenType.NUMBERLITERAL, position)
        val interpreter = Interpreter(printer, reader)

        val outputStream = ByteArrayOutputStream()
        System.setOut(PrintStream(outputStream))

        val result = interpreter.execute(node)

        val output = outputStream.toString().trim()

        assertTrue(output.contains("Asignando a la variable 'debugVar' el valor 30"))
        assertTrue(output.contains("Valor asignado a 'debugVar' es ahora 30"))
        assertEquals(30, result)
    }

    @Test
    fun `test valid readInput with string argument`() {
        val expression = LiteralNode("Enter your name: ", TokenType.STRINGLITERAL, position)
        val node = FunctionNode(TokenType.FUNCTION, "readInput", expression, position) // Modificado aquí

        // Simulamos la interacción con el reader
        val interpreter =
            Interpreter(
                printer,
                object : Reader {
                    override fun input(message: String): String = "John Doe"
                },
            )

        val result = interpreter.execute(node)

        assertEquals("John Doe", result) // Verifica que la entrada del usuario sea retornada correctamente
    }

    @Test
    fun `test readInput throws exception for non-string argument`() {
        val expression = LiteralNode("42", TokenType.NUMBERLITERAL, position)
        val node = FunctionNode(TokenType.FUNCTION, "readInput", expression, position) // Modificado aquí
        val interpreter = Interpreter(printer, reader)

        val exception =
            assertThrows(RuntimeException::class.java) {
                interpreter.execute(node)
            }

        assertEquals("El argumento de readInput debe ser String", exception.message)
    }

    @Test
    fun `test readInput throws exception for multiple arguments`() {
        val expression =
            BinaryNode(
                LiteralNode("Enter your age: ", TokenType.STRINGLITERAL, position),
                LiteralNode("5", TokenType.NUMBERLITERAL, position),
                Token(TokenType.OPERATOR, "+", position, position),
                position,
            )
        val node = FunctionNode(TokenType.FUNCTION, "readInput", expression, position) // Modificado aquí
        val interpreter = Interpreter(printer, reader)

        val exception =
            assertThrows(RuntimeException::class.java) {
                interpreter.execute(node)
            }

        assertEquals("readInput necesita solo un argumento", exception.message)
    }

    @Test
    fun `test readInput prints the argument message`() {
        val expression = LiteralNode("Enter a value: ", TokenType.STRINGLITERAL, position)
        val node = FunctionNode(TokenType.FUNCTION, "readInput", expression, position) // Modificado aquí

        val outputStream = ByteArrayOutputStream()
        System.setOut(PrintStream(outputStream))

        // Simulamos la interacción con el reader
        val interpreter =
            Interpreter(
                printer,
                object : Reader {
                    override fun input(message: String): String = "User input"
                },
            )

        interpreter.execute(node)

        val output = outputStream.toString().trim()
        assertTrue(output.contains("Enter a value:")) // Verifica que el mensaje se imprime
    }

    @Test
    fun `test NilNode returns null`() {
        // Crea un NilNode
        val nilNode = NilNode

        // Instancia el intérprete
        val interpreter = Interpreter(printer, reader)

        // Ejecuta el NilNode y verifica que el resultado es null
        val result = interpreter.execute(nilNode)
        assertNull(result, "NilNode should return null")
    }

    @Test
    fun `test interpreter version 1_0 throws on conditional`() {
        val version10Interpreter = Interpreter.forVersion("1.0", printer, reader)
        val condition = LiteralNode("true", TokenType.BOOLEANLITERAL, position)
        val thenBlock = PrintNode(LiteralNode("Condition is true", TokenType.STRINGLITERAL, position), position)
        val elseBlock = PrintNode(LiteralNode("Condition is false", TokenType.STRINGLITERAL, position), position)
        val node = ConditionalNode(condition, thenBlock, elseBlock, position)

        assertThrows(RuntimeException::class.java) {
            version10Interpreter.execute(node)
        }
    }

    @Test
    fun `test custom node evaluator plugin`() {
        val customEvaluator =
            object : NodeEvaluator {
                override fun canEvaluate(node: ASTNode): Boolean = node is NilNode

                override fun evaluate(
                    node: ASTNode,
                    interpreter: Interpreter,
                ): Any = "Custom Nil Evaluated!"
            }

        val interpreter = Interpreter(printer, reader, listOf(customEvaluator))
        val result = interpreter.execute(NilNode)
        assertEquals("Custom Nil Evaluated!", result)
    }

    @Test
    fun `test interpreter factory versions`() {
        val i10 = InterpreterFactory.forVersion("1.0", printer, reader)
        val i11 = InterpreterFactory.forVersion("1.1", printer, reader)
        assertEquals(true, i10.variables.isEmpty())
        assertEquals(true, i11.variables.isEmpty())
        assertThrows(IllegalArgumentException::class.java) {
            InterpreterFactory.forVersion("9.9", printer, reader)
        }
    }

    @Test
    fun `test declaration with nil node`() {
        val interpreter = Interpreter(printer, reader)
        val node =
            DeclarationNode(
                TokenType.KEYWORD,
                "let",
                "uninit",
                TokenType.DATA_TYPE,
                "number",
                NilNode,
                position,
            )
        interpreter.execute(node)
        assertEquals(false, interpreter.variables.containsKey("uninit"))
    }

    @Test
    fun `test handle readInput function`() {
        val testReader =
            object : Reader {
                override fun input(message: String): String = "100"
            }
        val interpreter = Interpreter(printer, testReader)
        val arg = LiteralNode("Enter number: ", TokenType.STRINGLITERAL, position)
        val node = FunctionNode(TokenType.FUNCTION, "readInput", arg, position)
        val result = interpreter.execute(node)
        assertEquals(100, result)
    }

    @Test
    fun `test readInput with invalid argument throws`() {
        val interpreter = Interpreter(printer, reader)
        val arg = NilNode
        val node = FunctionNode(TokenType.FUNCTION, "readInput", arg, position)
        assertThrows(RuntimeException::class.java) {
            interpreter.execute(node)
        }
    }

    @Test
    fun `test readEnv with invalid argument throws`() {
        val interpreter = Interpreter(printer, reader)
        val arg = NilNode
        val node = FunctionNode(TokenType.FUNCTION, "readEnv", arg, position)
        assertThrows(RuntimeException::class.java) {
            interpreter.execute(node)
        }
    }

    @Test
    fun `test unsupported node type throws`() {
        val interpreter = Interpreter(printer, reader, emptyList())
        val node = LiteralNode("test", TokenType.STRINGLITERAL, position)
        assertThrows(RuntimeException::class.java) {
            interpreter.execute(node)
        }
    }

    @Test
    fun `test conditional false without else block returns null`() {
        val interpreter = Interpreter(printer, reader)
        val condition = LiteralNode("false", TokenType.BOOLEANLITERAL, position)
        val thenBlock = PrintNode(LiteralNode("should not run", TokenType.STRINGLITERAL, position), position)
        val node = ConditionalNode(condition, thenBlock, null, position)
        val result = interpreter.execute(node)
        assertEquals(Unit, result)
    }

    @Test
    fun `test already declared variable throws`() {
        val interpreter = Interpreter(printer, reader)
        val expr = LiteralNode("1", TokenType.NUMBERLITERAL, position)
        val node1 = DeclarationNode(TokenType.KEYWORD, "let", "dupVar", TokenType.DATA_TYPE, "number", expr, position)
        val node2 = DeclarationNode(TokenType.KEYWORD, "let", "dupVar", TokenType.DATA_TYPE, "number", expr, position)
        interpreter.execute(node1)
        assertThrows(RuntimeException::class.java) {
            interpreter.execute(node2)
        }
    }

    @Test
    fun `test reassign const variable throws`() {
        val interpreter = Interpreter(printer, reader)
        interpreter.variables["c"] = 10
        interpreter.tiposDeVariables["c"] = "const"
        val assignNode =
            AssignationNode(
                "c",
                LiteralNode("20", TokenType.NUMBERLITERAL, position),
                TokenType.ASSIGNATION,
                position,
            )
        assertThrows(RuntimeException::class.java) {
            interpreter.execute(assignNode)
        }
    }

    @Test
    fun `test reassign variable wrong type throws`() {
        val interpreter = Interpreter(printer, reader)
        interpreter.variables["x"] = 10
        val assignNode =
            AssignationNode(
                "x",
                LiteralNode("hello", TokenType.STRINGLITERAL, position),
                TokenType.ASSIGNATION,
                position,
            )
        assertThrows(RuntimeException::class.java) {
            interpreter.execute(assignNode)
        }
    }

    @Test
    fun `test reassign string variable with number throws`() {
        val interpreter = Interpreter(printer, reader)
        interpreter.variables["str"] = "hello"
        val assignNode =
            AssignationNode(
                "str",
                LiteralNode("42", TokenType.NUMBERLITERAL, position),
                TokenType.ASSIGNATION,
                position,
            )
        assertThrows(RuntimeException::class.java) {
            interpreter.execute(assignNode)
        }
    }

    @Test
    fun `test reassign variable with unhandled type throws`() {
        val interpreter = Interpreter(printer, reader)
        interpreter.variables["boolVar"] = true
        val assignNode =
            AssignationNode(
                "boolVar",
                LiteralNode("false", TokenType.BOOLEANLITERAL, position),
                TokenType.ASSIGNATION,
                position,
            )
        assertThrows(RuntimeException::class.java) {
            interpreter.execute(assignNode)
        }
    }

    @Test
    fun `test binary operations with unsupported operands`() {
        val interpreter = Interpreter(printer, reader)
        val left = LiteralNode("true", TokenType.BOOLEANLITERAL, position)
        val right = LiteralNode("false", TokenType.BOOLEANLITERAL, position)
        val addNode = BinaryNode(left, right, Token(TokenType.OPERATOR, "+", position, position), position)
        val subNode = BinaryNode(left, right, Token(TokenType.OPERATOR, "-", position, position), position)
        assertThrows(RuntimeException::class.java) { interpreter.execute(addNode) }
        assertThrows(RuntimeException::class.java) { interpreter.execute(subNode) }
    }

    @Test
    fun `test custom function in function evaluator`() {
        val interpreter = Interpreter(printer, reader)
        val expr = LiteralNode("hello", TokenType.STRINGLITERAL, position)
        val funcNode = FunctionNode(TokenType.FUNCTION, "myCustomFunction", expr, position)
        val result = interpreter.execute(funcNode)
        assertEquals("hello", result)
    }
}
