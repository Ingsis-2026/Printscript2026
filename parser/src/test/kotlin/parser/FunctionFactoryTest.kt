package parser

import ast.FunctionNode
import ast.LiteralNode
import factories.FunctionFactory
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import token.Token
import token.TokenPosition
import token.TokenType
import kotlin.test.assertEquals

class FunctionFactoryTest {
    private val startPos = TokenPosition(0, 0)
    private val endPos = TokenPosition(0, 1)
    private val functionFactory = FunctionFactory()

    @Test
    fun `test valid function call`() {
        val tokens =
            listOf(
                Token(TokenType.FUNCTION, "print", startPos, endPos),
                Token(TokenType.PUNCTUATOR, "(", startPos, endPos),
                Token(TokenType.STRINGLITERAL, "\"Hello, World!\"", startPos, endPos),
                Token(TokenType.PUNCTUATOR, ")", startPos, endPos),
            )

        val result = functionFactory.createAST(tokens)
        assert(result is FunctionNode)
        result as FunctionNode
        assertEquals("print", result.functionName)
        assert(result.expression is LiteralNode)
        assertEquals("\"Hello, World!\"", (result.expression as LiteralNode).value)
    }

    @Test
    fun `test missing expression throws exception`() {
        val tokens =
            listOf(
                Token(TokenType.FUNCTION, "print", startPos, endPos),
                Token(TokenType.PUNCTUATOR, "(", startPos, endPos),
                Token(TokenType.PUNCTUATOR, ")", startPos, endPos),
            )

        val exception =
            assertThrows<NoSuchElementException> {
                functionFactory.createAST(tokens)
            }
        assertEquals("List is empty.", exception.message)
    }

    @Test
    fun `test canHandle with function token`() {
        val tokens =
            listOf(
                Token(TokenType.FUNCTION, "print", startPos, endPos),
                Token(TokenType.PUNCTUATOR, "(", startPos, endPos),
                Token(TokenType.STRINGLITERAL, "\"Hello, World!\"", startPos, endPos),
                Token(TokenType.PUNCTUATOR, ")", startPos, endPos),
            )

        assert(functionFactory.canHandle(tokens))
    }

    @Test
    fun `test canHandle without function token`() {
        val tokens =
            listOf(
                Token(TokenType.PUNCTUATOR, "(", startPos, endPos),
                Token(TokenType.STRINGLITERAL, "\"Hello, World!\"", startPos, endPos),
                Token(TokenType.PUNCTUATOR, ")", startPos, endPos),
            )

        assert(!functionFactory.canHandle(tokens))
    }
}