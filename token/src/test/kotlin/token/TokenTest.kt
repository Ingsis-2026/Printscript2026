package token

import kotlin.test.Test
import kotlin.test.assertEquals

class TokenTest {
    @Test
    fun testTokenCreation() {
        val startPos = TokenPosition(1, 1)
        val endPos = TokenPosition(1, 4)
        val token = Token(TokenType.KEYWORD, "let", startPos, endPos)

        assertEquals(TokenType.KEYWORD, token.getType())
        assertEquals("let", token.getValue())
        assertEquals(startPos, token.getPosition())
    }
}
