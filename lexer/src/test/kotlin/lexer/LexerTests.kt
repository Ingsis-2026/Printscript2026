import lexer.Lexer
import lexer.TokenMapper
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Test
import token.Token
import token.TokenPosition
import token.TokenType
import kotlin.test.assertEquals

class LexerTests {
    private val tkClassifier = TokenMapper("1.0")
    private val lexer = Lexer(tkClassifier)

    @Test
    fun `test first instruction`() {
        val input = "let name : string = \"Joe\"; "

        val result =
            listOf<Token>(
                Token(TokenType.KEYWORD, "let", TokenPosition(0, 0), TokenPosition(0, 3)),
                Token(TokenType.IDENTIFIER, "name", TokenPosition(0, 4), TokenPosition(0, 8)),
                Token(TokenType.DECLARATOR, ":", TokenPosition(0, 9), TokenPosition(0, 10)),
                Token(TokenType.DATA_TYPE, "string", TokenPosition(0, 11), TokenPosition(0, 17)),
                Token(TokenType.ASSIGNATION, "=", TokenPosition(0, 18), TokenPosition(0, 19)),
                Token(TokenType.STRINGLITERAL, "Joe", TokenPosition(0, 20), TokenPosition(0, 25)),
                Token(TokenType.PUNCTUATOR, ";", TokenPosition(0, 25), TokenPosition(0, 26)),
            )
        assertEquals(result, lexer.execute(input))
    }

    @Test
    fun `test multi-line input with new lines`() {
        val input =
            "let a : number = 12;\n" +
                "let b : number = 4;\n" +
                "a = a / b;\n"

        val result =
            listOf(
                Token(TokenType.KEYWORD, "let", TokenPosition(0, 0), TokenPosition(0, 3)),
                Token(TokenType.IDENTIFIER, "a", TokenPosition(0, 4), TokenPosition(0, 5)),
                Token(TokenType.DECLARATOR, ":", TokenPosition(0, 6), TokenPosition(0, 7)),
                Token(TokenType.DATA_TYPE, "number", TokenPosition(0, 8), TokenPosition(0, 14)),
                Token(TokenType.ASSIGNATION, "=", TokenPosition(0, 15), TokenPosition(0, 16)),
                Token(TokenType.NUMBERLITERAL, "12", TokenPosition(0, 17), TokenPosition(0, 19)),
                Token(TokenType.PUNCTUATOR, ";", TokenPosition(0, 19), TokenPosition(0, 20)),
                Token(TokenType.KEYWORD, "let", TokenPosition(1, 0), TokenPosition(1, 3)),
                Token(TokenType.IDENTIFIER, "b", TokenPosition(1, 4), TokenPosition(1, 5)),
                Token(TokenType.DECLARATOR, ":", TokenPosition(1, 6), TokenPosition(1, 7)),
                Token(TokenType.DATA_TYPE, "number", TokenPosition(1, 8), TokenPosition(1, 14)),
                Token(TokenType.ASSIGNATION, "=", TokenPosition(1, 15), TokenPosition(1, 16)),
                Token(TokenType.NUMBERLITERAL, "4", TokenPosition(1, 17), TokenPosition(1, 18)),
                Token(TokenType.PUNCTUATOR, ";", TokenPosition(1, 18), TokenPosition(1, 19)),
                Token(TokenType.IDENTIFIER, "a", TokenPosition(2, 0), TokenPosition(2, 1)),
                Token(TokenType.ASSIGNATION, "=", TokenPosition(2, 2), TokenPosition(2, 3)),
                Token(TokenType.IDENTIFIER, "a", TokenPosition(2, 4), TokenPosition(2, 5)),
                Token(TokenType.OPERATOR, "/", TokenPosition(2, 6), TokenPosition(2, 7)),
                Token(TokenType.IDENTIFIER, "b", TokenPosition(2, 8), TokenPosition(2, 9)),
                Token(TokenType.PUNCTUATOR, ";", TokenPosition(2, 9), TokenPosition(2, 10)),
            )

        assertEquals(result, lexer.execute(input))
    }

    @Test
    fun `test sum`() {
        val input = "(2 + 3);"
        val result =
            listOf(
                Token(TokenType.PARENTHESIS, "(", TokenPosition(0, 0), TokenPosition(0, 1)),
                Token(TokenType.NUMBERLITERAL, "2", TokenPosition(0, 1), TokenPosition(0, 2)),
                Token(TokenType.OPERATOR, "+", TokenPosition(0, 3), TokenPosition(0, 4)),
                Token(TokenType.NUMBERLITERAL, "3", TokenPosition(0, 5), TokenPosition(0, 6)),
                Token(TokenType.PARENTHESIS, ")", TokenPosition(0, 6), TokenPosition(0, 7)),
                Token(TokenType.PUNCTUATOR, ";", TokenPosition(0, 7), TokenPosition(0, 8)),
            )
        assertIterableEquals(result, lexer.execute(input))
    }

    @Test
    fun `test decimals`() {
        val input = "1.2"
        val result =
            listOf(
                Token(TokenType.NUMBERLITERAL, "1.2", TokenPosition(0, 0), TokenPosition(0, 3)),
            )
        assertEquals(result, lexer.execute(input))
    }

    @Test
    fun `test largeOperation`() {
        val input = "123 + 2 * 3 - 4 / 5"
        val result =
            listOf(
                Token(TokenType.NUMBERLITERAL, "123", TokenPosition(0, 0), TokenPosition(0, 3)),
                Token(TokenType.OPERATOR, "+", TokenPosition(0, 4), TokenPosition(0, 5)),
                Token(TokenType.NUMBERLITERAL, "2", TokenPosition(0, 6), TokenPosition(0, 7)),
                Token(TokenType.OPERATOR, "*", TokenPosition(0, 8), TokenPosition(0, 9)),
                Token(TokenType.NUMBERLITERAL, "3", TokenPosition(0, 10), TokenPosition(0, 11)),
                Token(TokenType.OPERATOR, "-", TokenPosition(0, 12), TokenPosition(0, 13)),
                Token(TokenType.NUMBERLITERAL, "4", TokenPosition(0, 14), TokenPosition(0, 15)),
                Token(TokenType.OPERATOR, "/", TokenPosition(0, 16), TokenPosition(0, 17)),
                Token(TokenType.NUMBERLITERAL, "5", TokenPosition(0, 18), TokenPosition(0, 19)),
            )

        assertEquals(result, lexer.execute(input))
    }

    // test changes
}
