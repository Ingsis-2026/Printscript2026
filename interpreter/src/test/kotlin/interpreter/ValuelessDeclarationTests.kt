package interpreter

import ast.AssignationNode
import ast.DeclarationNode
import ast.LiteralNode
import ast.NilNode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import token.TokenPosition
import token.TokenType

/**
 * Una variable declarada sin valor ya está declarada, y las dos reglas que dependen de eso
 * tienen que verla: no se la puede volver a declarar, y si es `const` no se la puede asignar.
 *
 * Las dos reglas preguntaban si la variable tenía valor, que es un hecho distinto: una
 * declaración sin valor no lo cumple y se colaba por el medio.
 */
class ValuelessDeclarationTests {
    private val position = TokenPosition(0, 0)
    private val printer =
        object : Printer {
            override fun print(message: String) = Unit
        }
    private val reader =
        object : Reader {
            override fun input(message: String): String = ""
        }

    private fun valuelessDeclaration(
        name: String,
        type: String,
        keyword: String = "let",
    ) = DeclarationNode(TokenType.KEYWORD, keyword, name, TokenType.DATA_TYPE, type, NilNode, position)

    private fun assignment(
        name: String,
        value: String,
        type: TokenType,
    ) = AssignationNode(name, LiteralNode(value, type, position), TokenType.ASSIGNATION, position)

    @Test
    fun `redeclaring a variable that was declared without a value fails`() {
        val interpreter = Interpreter(printer, reader)
        interpreter.execute(valuelessDeclaration("x", "number"))

        val exception =
            assertThrows<InterpreterException> {
                interpreter.execute(valuelessDeclaration("x", "string"))
            }

        assertEquals("La variable 'x' ya ha sido declarada", exception.message)
    }

    @Test
    fun `a rejected redeclaration leaves the original declared type untouched`() {
        val interpreter = Interpreter(printer, reader)
        interpreter.execute(valuelessDeclaration("x", "number"))

        assertThrows<InterpreterException> {
            interpreter.execute(valuelessDeclaration("x", "string"))
        }

        assertEquals("number", interpreter.variables.declarationOf("x")?.declaredType)
    }

    @Test
    fun `assigning to a const declared without a value fails`() {
        val interpreter = Interpreter(printer, reader)
        interpreter.execute(valuelessDeclaration("c", "number", keyword = "const"))

        val exception =
            assertThrows<InterpreterException> {
                interpreter.execute(assignment("c", "20", TokenType.NUMBERLITERAL))
            }

        assertEquals("No es posible reasignar una variable de tipo const", exception.message)
    }

    @Test
    fun `a variable declared without a value can still receive its first value`() {
        val interpreter = Interpreter(printer, reader)
        interpreter.execute(valuelessDeclaration("n", "number"))

        interpreter.execute(assignment("n", "7", TokenType.NUMBERLITERAL))

        assertEquals(7, interpreter.variables.valueOf("n"))
    }
}
