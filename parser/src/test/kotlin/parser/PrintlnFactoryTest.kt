package parser

import ast.BinaryNode
import ast.LiteralNode
import ast.PrintNode
import factories.PrintlnFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import token.Token
import token.TokenPosition
import token.TokenType

class PrintlnFactoryTest {
    private val startPos = TokenPosition(0, 0)
    private val endPos = TokenPosition(0, 1)
    private val factory = PrintlnFactory()

    @Test
    fun `test valid println with single literal`() {
        val tokens =
            listOf(
                Token(TokenType.FUNCTION, "println", startPos, endPos),
                Token(TokenType.PARENTHESIS, "(", startPos, endPos),
                Token(TokenType.NUMBERLITERAL, "42", startPos, endPos),
                Token(TokenType.PARENTHESIS, ")", startPos, endPos),
            )
        val result = factory.createAST(tokens)

        assertTrue(result is PrintNode)
        val printNode = result as PrintNode
        assertTrue(printNode.expression is LiteralNode)
        assertEquals("42", (printNode.expression as LiteralNode).value)
    }

    @Test
    fun `test valid println with expression`() {
        val tokens =
            listOf(
                Token(TokenType.FUNCTION, "println", startPos, endPos),
                Token(TokenType.PARENTHESIS, "(", startPos, endPos),
                Token(TokenType.NUMBERLITERAL, "1", startPos, endPos),
                Token(TokenType.OPERATOR, "+", startPos, endPos),
                Token(TokenType.NUMBERLITERAL, "2", startPos, endPos),
                Token(TokenType.PARENTHESIS, ")", startPos, endPos),
            )
        val result = factory.createAST(tokens)

        assertTrue(result is PrintNode)
        val printNode = result as PrintNode
        assertTrue(printNode.expression is BinaryNode)
        // Additional assertions can be made to check the structure of the expression
    }
}
