package parser

import ast.BinaryNode
import ast.LiteralNode
import factories.OperationFactory
import lexer.Lexer
import lexer.TokenMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import token.Token
import token.TokenPosition
import token.TokenType

class OperationFactoryTest {
    private val startPos = TokenPosition(0, 0)
    private val endPos = TokenPosition(0, 1)
    private val factory = OperationFactory()

    @Test
    fun `test single literal`() {
        val tokens = listOf(Token(TokenType.NUMBERLITERAL, "5", startPos, endPos))
        val result = factory.createAST(tokens)
        assertTrue(result is LiteralNode)
        assertEquals("5", (result as LiteralNode).value)
    }

    @Test
    fun `test simple addition`() {
        val tokens =
            listOf(
                Token(TokenType.NUMBERLITERAL, "3", startPos, endPos),
                Token(TokenType.OPERATOR, "+", startPos, endPos),
                Token(TokenType.NUMBERLITERAL, "4", startPos, endPos),
            )
        val result = factory.createAST(tokens)
        assertTrue(result is BinaryNode)
        assertEquals("+", (result as BinaryNode).operator.value)
        assertEquals("3", (result.left as LiteralNode).value)
        assertEquals("4", (result.right as LiteralNode).value)
    }

    @Test
    fun `test addition with parentheses`() {
        val tokens =
            listOf(
                Token(TokenType.PARENTHESIS, "(", startPos, endPos),
                Token(TokenType.NUMBERLITERAL, "3", startPos, endPos),
                Token(TokenType.OPERATOR, "+", startPos, endPos),
                Token(TokenType.NUMBERLITERAL, "4", startPos, endPos),
                Token(TokenType.PARENTHESIS, ")", startPos, endPos),
            )
        val result = factory.createAST(tokens)
        assertTrue(result is BinaryNode)
        assertEquals("+", (result as BinaryNode).operator.value)
        assertEquals("3", (result.left as LiteralNode).value)
        assertEquals("4", (result.right as LiteralNode).value)
    }

    @Test
    fun `test multiplication has higher precedence than addition`() {
        val tokens =
            listOf(
                Token(TokenType.NUMBERLITERAL, "2", startPos, endPos),
                Token(TokenType.OPERATOR, "+", startPos, endPos),
                Token(TokenType.NUMBERLITERAL, "3", startPos, endPos),
                Token(TokenType.OPERATOR, "*", startPos, endPos),
                Token(TokenType.NUMBERLITERAL, "4", startPos, endPos),
            )
        val result = factory.createAST(tokens)

        assertTrue(result is BinaryNode)
        assertEquals("+", (result as BinaryNode).operator.value)
        assertEquals("2", (result.left as LiteralNode).value)

        // Verify the right side is another BinaryNode for the multiplication
        assertTrue(result.right is BinaryNode)
        val rightNode = result.right as BinaryNode
        assertEquals("*", rightNode.operator.value)
        assertEquals("3", (rightNode.left as LiteralNode).value)
        assertEquals("4", (rightNode.right as LiteralNode).value)
    }

    private fun parseExpression(source: String) = factory.createAST(Lexer(TokenMapper("1.0")).execute(source))

    @Test
    fun `a string that reads like a parenthesis is an operand`() {
        val result = parseExpression("\"(\" + 1") as BinaryNode

        assertEquals("+", result.operator.value)
        assertEquals(TokenType.STRINGLITERAL, (result.left as LiteralNode).type)
        assertEquals("(", (result.left as LiteralNode).value)
    }

    @Test
    fun `a string that reads like an operator is an operand`() {
        val result = parseExpression("\"a\" + \"+\" + \"b\"") as BinaryNode

        val middle = (result.right as BinaryNode).left as LiteralNode
        assertEquals(TokenType.STRINGLITERAL, middle.type)
        assertEquals("+", middle.value)
    }

    @Test
    fun `real parentheses still group`() {
        val result = parseExpression("(1 + 2) * 3") as BinaryNode

        assertEquals("*", result.operator.value)
        assertEquals("+", (result.left as BinaryNode).operator.value)
    }
}
