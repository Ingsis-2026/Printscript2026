package lexer

import lexer.mapper.TokenMapper
import token.TokenType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LexerTest {
    @Test
    fun testLexerVersion10SimpleDeclaration() {
        val mapper = TokenMapper("1.0")
        val lexer = Lexer(mapper)
        val input = "let a: number = 5;"

        val tokens = lexer.execute(input)
        assertTrue(tokens.isNotEmpty())
        assertEquals(TokenType.KEYWORD, tokens[0].getType())
        assertEquals("let", tokens[0].getValue())
        assertEquals(TokenType.IDENTIFIER, tokens[1].getType())
        assertEquals("a", tokens[1].getValue())
        assertEquals(TokenType.DECLARATOR, tokens[2].getType())
        assertEquals(":", tokens[2].getValue())
        assertEquals(TokenType.DATA_TYPE, tokens[3].getType())
        assertEquals("number", tokens[3].getValue())
        assertEquals(TokenType.ASSIGNATION, tokens[4].getType())
        assertEquals("=", tokens[4].getValue())
        assertEquals(TokenType.NUMBERLITERAL, tokens[5].getType())
        assertEquals("5", tokens[5].getValue())
        assertEquals(TokenType.PUNCTUATOR, tokens[6].getType())
        assertEquals(";", tokens[6].getValue())
    }

    @Test
    fun testLexerVersion11ConstDeclaration() {
        val mapper = TokenMapper("1.1")
        val lexer = Lexer(mapper)
        val input = "const b: string = \"hello\";"

        val tokens = lexer.execute(input)
        assertTrue(tokens.isNotEmpty())
        assertEquals(TokenType.KEYWORD, tokens[0].getType())
        assertEquals("const", tokens[0].getValue())
        assertEquals(TokenType.IDENTIFIER, tokens[1].getType())
        assertEquals("b", tokens[1].getValue())
        assertEquals(TokenType.STRINGLITERAL, tokens[5].getType())
        assertEquals("hello", tokens[5].getValue())
    }
}
