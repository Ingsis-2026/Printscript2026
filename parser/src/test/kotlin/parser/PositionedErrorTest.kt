package parser

import ast.ConditionalNode
import ast.DeclarationNode
import ast.PrintNode
import factories.AssignationFactory
import factories.ConditionalFactory
import factories.OperationFactory
import factories.PrintlnFactory
import lexer.Lexer
import lexer.TokenMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import token.Token
import token.TokenPosition
import token.TokenType

/**
 * Todo error de parseo debe llegar a la CLI con su ubicación, y el flujo de sentencias debe
 * seguir funcionando con bloques (`if` / `else`), que es donde la máquina de estados del
 * [StatementSplitter] cuenta llaves.
 */
class PositionedErrorTest {
    private val lexer11 = Lexer(TokenMapper("1.1"))
    private val parser11 = Parser.forVersion("1.1")
    private val startPos = TokenPosition(0, 0)
    private val endPos = TokenPosition(0, 4)

    private fun token(
        type: TokenType,
        value: String,
    ) = Token(type, value, startPos, endPos)

    // --- Bloques: el splitter debe cortar por llaves, no por ";" ---

    @Test
    fun `a conditional with else is emitted as a single statement`() {
        val nodes = parser11.execute(lexer11.execute("if (x) { println(\"yes\") } else { println(\"no\") }")).toList()

        assertEquals(1, nodes.size)
        assertTrue(nodes[0] is ConditionalNode)
    }

    @Test
    fun `a conditional without else is emitted as a single statement`() {
        val nodes = parser11.execute(lexer11.execute("if (x) { println(\"yes\") }")).toList()

        assertEquals(1, nodes.size)
        assertTrue(nodes[0] is ConditionalNode)
    }

    @Test
    fun `statements around a conditional are split correctly`() {
        val source = "let a : number = 1; if (x) { println(\"yes\") } println(a);"

        val nodes = parser11.execute(lexer11.execute(source)).toList()

        assertEquals(3, nodes.size)
        assertTrue(nodes[0] is DeclarationNode)
        assertTrue(nodes[1] is ConditionalNode)
        assertTrue(nodes[2] is PrintNode)
    }

    @Test
    fun `a conditional can be streamed without reading past it`() {
        val lines =
            sequenceOf(
                "if (x) { println(\"yes\") }",
                "let unused : number = 1;",
            )

        val first = parser11.execute(lexer11.convertToTokens(lines)).first()

        assertTrue(first is ConditionalNode)
    }

    // --- Errores con posición ---

    @Test
    fun `an unbalanced block reports its span`() {
        val tokens =
            listOf(
                token(TokenType.CONDITIONAL, "if"),
                token(TokenType.PARENTHESIS, "("),
                token(TokenType.BOOLEANLITERAL, "true"),
                token(TokenType.PARENTHESIS, ")"),
                token(TokenType.PUNCTUATOR, "{"),
            )

        val exception = assertThrows<ParserException> { ConditionalFactory().createAST(tokens) }

        assertEquals("Error parsing block: missing or unbalanced braces", exception.message)
        assertNotNull(exception.startPosition)
        assertNotNull(exception.endPosition)
    }

    @Test
    fun `println with too few tokens reports a position`() {
        val tokens = listOf(token(TokenType.FUNCTION, "println"), token(TokenType.PARENTHESIS, "("))

        val exception = assertThrows<ParserException> { PrintlnFactory().createAST(tokens) }

        assertTrue(exception.message!!.contains("Too few tokens"), exception.message)
        assertEquals(startPos, exception.startPosition)
    }

    @Test
    fun `println with misordered parentheses reports a position`() {
        val tokens =
            listOf(
                token(TokenType.FUNCTION, "println"),
                token(TokenType.PARENTHESIS, ")"),
                token(TokenType.NUMBERLITERAL, "1"),
                token(TokenType.PARENTHESIS, "("),
            )

        val exception = assertThrows<ParserException> { PrintlnFactory().createAST(tokens) }

        assertTrue(exception.message!!.contains("Missing or misordered parentheses"), exception.message)
        assertEquals(startPos, exception.startPosition)
    }

    @Test
    fun `println with an empty expression reports a position`() {
        val tokens =
            listOf(
                token(TokenType.FUNCTION, "println"),
                token(TokenType.PARENTHESIS, "("),
                token(TokenType.PARENTHESIS, ")"),
                token(TokenType.PUNCTUATOR, ";"),
            )

        val exception = assertThrows<ParserException> { PrintlnFactory().createAST(tokens) }

        assertTrue(exception.message!!.contains("empty expression"), exception.message)
        assertEquals(startPos, exception.startPosition)
    }

    @Test
    fun `a malformed operation reports the span of the expression`() {
        val tokens =
            listOf(
                token(TokenType.NUMBERLITERAL, "1"),
                token(TokenType.NUMBERLITERAL, "2"),
            )

        val exception = assertThrows<ParserException> { OperationFactory().createAST(tokens) }

        assertEquals("Error in operation", exception.message)
        assertEquals(startPos, exception.startPosition)
        assertEquals(endPos, exception.endPosition)
    }

    @Test
    fun `an unhandled sentence reports its span`() {
        val exception =
            assertThrows<ParserException> {
                parser11.execute(lexer11.execute("42;")).toList()
            }

        assertEquals("Can't handle this sentence", exception.message)
        assertEquals(0, exception.startPosition?.row)
    }

    @Test
    fun `an assignation without an assignation token is rejected`() {
        val tokens = listOf(token(TokenType.IDENTIFIER, "x"), token(TokenType.NUMBERLITERAL, "1"))

        assertThrows<Exception> { AssignationFactory().createAST(tokens) }
    }
}
