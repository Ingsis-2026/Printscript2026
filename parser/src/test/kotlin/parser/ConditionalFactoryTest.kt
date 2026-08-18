package parser

import factories.ConditionalFactory
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import token.Token
import token.TokenPosition
import token.TokenType

class ConditionalFactoryTest {
    private val startPos = TokenPosition(0, 0)
    private val endPos = TokenPosition(0, 1)
    private val factory = ConditionalFactory()

    @Test
    fun `test createAST with invalid token structure`() {
        // Missing closing brace for the block
        val tokens =
            listOf(
                Token(TokenType.CONDITIONAL, "if", startPos, endPos),
                Token(TokenType.PARENTHESIS, "(", startPos, endPos),
                Token(TokenType.NUMBERLITERAL, "1", startPos, endPos),
                Token(TokenType.PARENTHESIS, ")", startPos, endPos),
                Token(TokenType.PUNCTUATOR, "{", startPos, endPos),
                Token(TokenType.NUMBERLITERAL, "2", startPos, endPos),
                // No closing brace
            )

        assertThrows<RuntimeException> {
            factory.createAST(tokens)
        }
    }

    @Test
    fun `test createAST with missing parentheses`() {
        // Missing parentheses
        val tokens =
            listOf(
                Token(TokenType.CONDITIONAL, "if", startPos, endPos),
                Token(TokenType.NUMBERLITERAL, "1", startPos, endPos),
                Token(TokenType.PUNCTUATOR, "{", startPos, endPos),
                Token(TokenType.NUMBERLITERAL, "2", startPos, endPos),
                Token(TokenType.PUNCTUATOR, "}", startPos, endPos),
            )

        assertThrows<Exception> { // Change this to the appropriate exception if needed
            factory.createAST(tokens)
        }
    }
}