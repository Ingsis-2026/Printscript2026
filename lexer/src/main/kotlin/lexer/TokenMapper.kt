package lexer

import token.TokenType

/**
 * Los tokens que existen en una versión de PrintScript, en el orden en que el [Lexer] los prueba.
 *
 * Una palabra que la versión no tiene no se rechaza acá: no está en la lista, así que se lee como
 * identificador, y es el parser el que rechaza la sentencia.
 */
class TokenMapper(
    val definitions: List<TokenDefinition>,
) {
    constructor(version: String) : this(definitionsFor(version))

    companion object {
        fun forVersion(version: String): TokenMapper = TokenMapper(version)

        private fun definitionsFor(version: String): List<TokenDefinition> =
            when (version) {
                "1.0" -> inOrder(VERSION_1_0_WORDS, VERSION_1_0_SYMBOLS)
                "1.1" -> inOrder(VERSION_1_1_WORDS, VERSION_1_1_SYMBOLS)
                else -> throw IllegalArgumentException("Unsupported version: $version")
            }

        /** Las palabras van antes que el identificador porque él también las reconocería, y gana la primera. */
        private fun inOrder(
            words: List<TokenDefinition>,
            symbols: List<TokenDefinition>,
        ): List<TokenDefinition> = words + NAMES_AND_LITERALS + symbols

        private val VERSION_1_0_WORDS =
            listOf(
                TokenDefinition.words(TokenType.KEYWORD, "let"),
                TokenDefinition.words(TokenType.FUNCTION, "println"),
                TokenDefinition.words(TokenType.DATA_TYPE, "string", "number"),
            )

        private val VERSION_1_1_WORDS =
            VERSION_1_0_WORDS +
                listOf(
                    TokenDefinition.words(TokenType.KEYWORD, "const"),
                    TokenDefinition.words(TokenType.CONDITIONAL, "if", "else"),
                    TokenDefinition.words(TokenType.FUNCTION, "readInput", "readEnv"),
                    TokenDefinition.words(TokenType.DATA_TYPE, "boolean"),
                    TokenDefinition.words(TokenType.BOOLEANLITERAL, "true", "false"),
                )

        private val NAMES_AND_LITERALS =
            listOf(
                TokenDefinition(TokenType.IDENTIFIER, """\b[a-zA-Z_][a-zA-Z0-9_]*\b""".toRegex()),
                TokenDefinition(TokenType.STRINGLITERAL, "'[^']*'|\"[^\"]*\"".toRegex()),
                TokenDefinition(TokenType.NUMBERLITERAL, """[0-9]+(\.[0-9]+)?""".toRegex()),
            )

        private val VERSION_1_0_SYMBOLS =
            listOf(
                TokenDefinition(TokenType.PARENTHESIS, "[()]".toRegex()),
                TokenDefinition(TokenType.DECLARATOR, ":".toRegex()),
                // Antes que los operadores, que también aceptan `=`: si no, `x=-1` daría un operador `=-`.
                TokenDefinition(TokenType.ASSIGNATION, "=".toRegex()),
                TokenDefinition(TokenType.OPERATOR, "[-+*/%=><!&|^~]+".toRegex()),
                TokenDefinition(TokenType.PUNCTUATOR, """[\[\],;.]""".toRegex()),
            )

        private val VERSION_1_1_SYMBOLS = VERSION_1_0_SYMBOLS + TokenDefinition(TokenType.PUNCTUATOR, "[{}]".toRegex())
    }
}
