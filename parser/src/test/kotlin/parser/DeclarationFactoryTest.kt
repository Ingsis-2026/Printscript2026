package parser

import ast.DeclarationNode
import ast.LiteralNode
import ast.NilNode
import factories.DeclarationFactory
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import token.Token
import token.TokenPosition
import token.TokenType
import kotlin.test.assertEquals

class DeclarationFactoryTest {
    private val startPos = TokenPosition(0, 0)
    private val endPos = TokenPosition(0, 1)
    private val declarationFactory = DeclarationFactory()

    @Test
    fun `test valid number declaration`() {
        val tokens =
            listOf(
                Token(TokenType.KEYWORD, "let", startPos, endPos),
                Token(TokenType.IDENTIFIER, "x", startPos, endPos),
                Token(TokenType.DATA_TYPE, "number", startPos, endPos),
                Token(TokenType.PUNCTUATOR, "=", startPos, endPos),
                Token(TokenType.NUMBERLITERAL, "42", startPos, endPos),
            )

        val result = declarationFactory.createAST(tokens)
        assert(result is DeclarationNode)
        assertEquals("x", (result as DeclarationNode).id)
        assertEquals("42", (result.expr as LiteralNode).value)
    }

    @Test
    fun `test valid string declaration`() {
        val tokens =
            listOf(
                Token(TokenType.KEYWORD, "let", startPos, endPos),
                Token(TokenType.IDENTIFIER, "text", startPos, endPos),
                Token(TokenType.DATA_TYPE, "string", startPos, endPos),
                Token(TokenType.PUNCTUATOR, "=", startPos, endPos),
                Token(TokenType.STRINGLITERAL, "\"Hello\"", startPos, endPos),
            )

        val result = declarationFactory.createAST(tokens)
        assert(result is DeclarationNode)
        assertEquals("text", (result as DeclarationNode).id)
        assertEquals("\"Hello\"", (result.expr as LiteralNode).value)
    }

    @Test
    fun `test valid boolean declaration`() {
        val tokens =
            listOf(
                Token(TokenType.KEYWORD, "let", startPos, endPos),
                Token(TokenType.IDENTIFIER, "flag", startPos, endPos),
                Token(TokenType.DATA_TYPE, "boolean", startPos, endPos),
                Token(TokenType.PUNCTUATOR, "=", startPos, endPos),
                Token(TokenType.BOOLEANLITERAL, "true", startPos, endPos),
            )

        val result = declarationFactory.createAST(tokens)
        assert(result is DeclarationNode)
        assertEquals("flag", (result as DeclarationNode).id)
        assertEquals("true", (result.expr as LiteralNode).value)
    }

    @Test
    fun `test type inconsistency number with string literal`() {
        val tokens =
            listOf(
                Token(TokenType.KEYWORD, "let", startPos, endPos),
                Token(TokenType.IDENTIFIER, "num", startPos, endPos),
                Token(TokenType.DATA_TYPE, "number", startPos, endPos),
                Token(TokenType.PUNCTUATOR, "=", startPos, endPos),
                Token(TokenType.STRINGLITERAL, "\"Not a number\"", startPos, endPos),
            )

        val exception =
            assertThrows<Exception> {
                declarationFactory.createAST(tokens)
            }
        assertEquals("declared data type number is inconsistent with the expression", exception.message)
    }

    @Test
    fun `test type inconsistency string with number literal`() {
        val tokens =
            listOf(
                Token(TokenType.KEYWORD, "let", startPos, endPos),
                Token(TokenType.IDENTIFIER, "str", startPos, endPos),
                Token(TokenType.DATA_TYPE, "string", startPos, endPos),
                Token(TokenType.PUNCTUATOR, "=", startPos, endPos),
                Token(TokenType.NUMBERLITERAL, "123", startPos, endPos),
            )

        val exception =
            assertThrows<Exception> {
                declarationFactory.createAST(tokens)
            }
        assertEquals("declared data type string is inconsistent with the expression", exception.message)
    }

    @Test
    fun `test missing identifier token throws exception`() {
        val tokens =
            listOf(
                Token(TokenType.KEYWORD, "let", startPos, endPos),
                Token(TokenType.DATA_TYPE, "number", startPos, endPos),
                Token(TokenType.PUNCTUATOR, "=", startPos, endPos),
                Token(TokenType.NUMBERLITERAL, "42", startPos, endPos),
            )

        val exception =
            assertThrows<IllegalArgumentException> {
                declarationFactory.createAST(tokens)
            }
        assertEquals("Expected an IDENTIFIER token but found none.", exception.message)
    }

    @Test
    fun `test missing data type token throws exception`() {
        val tokens =
            listOf(
                Token(TokenType.KEYWORD, "let", startPos, endPos),
                Token(TokenType.IDENTIFIER, "x", startPos, endPos),
                Token(TokenType.PUNCTUATOR, "=", startPos, endPos),
                Token(TokenType.NUMBERLITERAL, "42", startPos, endPos),
            )

        val exception =
            assertThrows<IllegalArgumentException> {
                declarationFactory.createAST(tokens)
            }
        assertEquals("Expected a DATA_TYPE or DATA_TYPE token but found none.", exception.message)
    }

    @Test
    fun `test declaration without expression`() {
        val tokens =
            listOf(
                Token(TokenType.KEYWORD, "let", startPos, endPos),
                Token(TokenType.IDENTIFIER, "y", startPos, endPos),
                Token(TokenType.DATA_TYPE, "number", startPos, endPos),
            )

        val result = declarationFactory.createAST(tokens)
        assert(result is DeclarationNode)
        assertEquals("y", (result as DeclarationNode).id)
        assert(result.expr is NilNode)
    }
}