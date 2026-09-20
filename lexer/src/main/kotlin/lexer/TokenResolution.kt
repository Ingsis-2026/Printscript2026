package lexer

import token.TokenType

/** Resultado de resolver un lexema contra las reglas de una versión: qué token es, o por qué no lo es. */
sealed class TokenResolution {
    data class Recognized(
        val type: TokenType,
    ) : TokenResolution()

    data class Rejected(
        val reason: String,
    ) : TokenResolution()
}
