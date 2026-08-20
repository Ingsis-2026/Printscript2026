import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import token.Token
import token.TokenPosition
import token.TokenType

class TokenTests {
    @Test
    fun `test Token creation and properties`() {
        val initialPosition = TokenPosition(1, 5)
        val finalPosition = TokenPosition(1, 5)
        val token = Token(TokenType.LITERAL, "42", initialPosition, finalPosition)

        assertEquals(TokenType.LITERAL, token.getType())
        assertEquals("42", token.value)
        assertEquals(initialPosition, token.getPosition()) // Esto se debe cambiar para que se ajuste a tus expectativas
        assertEquals(finalPosition, token.getPosition()) // Acceder directamente a la propiedad pública
    }

    @Test
    fun `test equals and hashCode`() {
        val initialPosition = TokenPosition(1, 5)
        val finalPosition = TokenPosition(1, 10)
        val token1 = Token(TokenType.LITERAL, "42", initialPosition, finalPosition)
        val token2 = Token(TokenType.LITERAL, "42", initialPosition, finalPosition)
        val token3 = Token(TokenType.OPERATOR, "+", initialPosition, finalPosition)

        // Prueba de igualdad
        assertEquals(token1, token2) // Deben ser iguales
        assertNotEquals(token1, token3) // Deben ser diferentes

        // Prueba de hashCode
        assertEquals(token1.hashCode(), token2.hashCode()) // Deben tener el mismo hashCode
        assertNotEquals(token1.hashCode(), token3.hashCode()) // Deben tener diferentes hashCode
    }

    @Test
    fun `test toString`() {
        val initialPosition = TokenPosition(1, 5)
        val finalPosition = TokenPosition(1, 10)
        val token = Token(TokenType.LITERAL, "42", initialPosition, finalPosition)

        val expectedString =
            "Token(type = 'LITERAL', value = '42', start = 'TokenPosition(row=1, column=5)'," +
                " end = 'TokenPosition(row=1, column=10)')"
        assertEquals(expectedString, token.toString())
    }
}
