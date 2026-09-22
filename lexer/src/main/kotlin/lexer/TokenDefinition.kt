package lexer

import token.TokenType

/** Un tipo de token y el regex que reconoce sus lexemas. */
class TokenDefinition(
    val type: TokenType,
    val regex: Regex,
) {
    companion object {
        /** Palabras enteras: el `\b` de cada lado hace que `let` no se reconozca adentro de `letter`. */
        fun words(
            type: TokenType,
            vararg words: String,
        ): TokenDefinition = TokenDefinition(type, words.joinToString("|") { """\b$it\b""" }.toRegex())
    }
}
