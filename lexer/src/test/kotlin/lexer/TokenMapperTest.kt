package lexer

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import token.TokenType

class TokenMapperTest {
    private fun typesOf(
        version: String,
        source: String,
    ): List<TokenType> = Lexer(TokenMapper(version)).execute(source).map { it.getType() }

    @Test
    fun `test the words of version 1,0`() {
        assertEquals(
            listOf(TokenType.KEYWORD, TokenType.FUNCTION, TokenType.DATA_TYPE, TokenType.DATA_TYPE),
            typesOf("1.0", "let println string number"),
        )
    }

    @Test
    fun `test the words version 1,1 adds`() {
        assertEquals(
            listOf(
                TokenType.KEYWORD,
                TokenType.CONDITIONAL,
                TokenType.CONDITIONAL,
                TokenType.FUNCTION,
                TokenType.FUNCTION,
                TokenType.DATA_TYPE,
                TokenType.BOOLEANLITERAL,
                TokenType.BOOLEANLITERAL,
            ),
            typesOf("1.1", "const if else readInput readEnv boolean true false"),
        )
    }

    @Test
    fun `test in version 1,0 the words of 1,1 are plain identifiers`() {
        assertEquals(
            List(8) { TokenType.IDENTIFIER },
            typesOf("1.0", "const if else readInput readEnv boolean true false"),
        )
    }

    @Test
    fun `test a word inside a longer name is part of the identifier`() {
        assertEquals(
            List(5) { TokenType.IDENTIFIER },
            typesOf("1.1", "letter printlnCount readInputValue trueish iffy"),
        )
    }

    @Test
    fun `test assignation is found before an operator that starts with the same character`() {
        assertEquals(
            listOf(TokenType.IDENTIFIER, TokenType.ASSIGNATION, TokenType.OPERATOR, TokenType.NUMBERLITERAL),
            typesOf("1.0", "x=-1"),
        )
    }

    @Test
    fun `test an operator can have more than one character`() {
        assertEquals(listOf(TokenType.OPERATOR), typesOf("1.0", "<="))
    }

    @Test
    fun `test literals`() {
        assertEquals(
            listOf(TokenType.STRINGLITERAL, TokenType.STRINGLITERAL, TokenType.NUMBERLITERAL, TokenType.NUMBERLITERAL),
            typesOf("1.0", "'single' \"double\" 3.14 7"),
        )
    }

    @Test
    fun `test braces exist only in version 1,1`() {
        assertEquals(listOf(TokenType.PUNCTUATOR, TokenType.PUNCTUATOR), typesOf("1.1", "{}"))
        assertThrows<LexerException> { typesOf("1.0", "{") }
    }

    @Test
    fun `test unsupported version throws exception`() {
        val exception =
            assertThrows<IllegalArgumentException> {
                TokenMapper("2.0")
            }
        assertEquals("Unsupported version: 2.0", exception.message)
    }

    @Test
    fun `test custom definitions injection`() {
        val lexer =
            Lexer(
                TokenMapper(
                    listOf(
                        TokenDefinition.words(TokenType.KEYWORD, "custom"),
                        TokenDefinition(TokenType.NUMBERLITERAL, """\d+""".toRegex()),
                    ),
                ),
            )

        assertEquals(listOf(TokenType.KEYWORD, TokenType.NUMBERLITERAL), lexer.execute("custom 456").map { it.getType() })
        assertThrows<LexerException> { lexer.execute("let") }
    }
}
