package lexer

import token.TokenType

/**
 * El vocabulario de una versión de PrintScript: sus palabras y sus símbolos, con el tipo de token
 * que produce cada uno.
 *
 * Una palabra que la versión no tiene no está en [words], así que el [Lexer] la lee como
 * identificador, y es el parser el que rechaza la sentencia.
 */
class TokenMapper(
    val words: Map<String, TokenType>,
    val symbols: Map<String, TokenType>,
) {
    constructor(version: String) : this(vocabularyOf(version))

    private constructor(vocabulary: TokenMapper) : this(vocabulary.words, vocabulary.symbols)

    private val symbolsLongestFirst = symbols.entries.sortedByDescending { it.key.length }

    /** El símbolo más largo de la versión que aparece en [line] a partir de [column], o `null` si ninguno. */
    fun symbolAt(
        line: String,
        column: Int,
    ): Map.Entry<String, TokenType>? = symbolsLongestFirst.firstOrNull { line.startsWith(it.key, column) }

    /** Una versión que agrega palabras y símbolos a esta. */
    operator fun plus(addition: TokenMapper): TokenMapper = TokenMapper(words + addition.words, symbols + addition.symbols)

    companion object {
        fun forVersion(version: String): TokenMapper = TokenMapper(version)

        private fun vocabularyOf(version: String): TokenMapper =
            when (version) {
                "1.0" -> VERSION_1_0
                "1.1" -> VERSION_1_1
                else -> throw IllegalArgumentException("Unsupported version: $version")
            }

        private val VERSION_1_0 =
            TokenMapper(
                words =
                    mapOf(
                        "let" to TokenType.KEYWORD,
                        "println" to TokenType.FUNCTION,
                        "string" to TokenType.DATA_TYPE,
                        "number" to TokenType.DATA_TYPE,
                    ),
                symbols =
                    mapOf(
                        "(" to TokenType.PARENTHESIS,
                        ")" to TokenType.PARENTHESIS,
                        ":" to TokenType.DECLARATOR,
                        "=" to TokenType.ASSIGNATION,
                        ";" to TokenType.PUNCTUATOR,
                        "+" to TokenType.OPERATOR,
                        "-" to TokenType.OPERATOR,
                        "*" to TokenType.OPERATOR,
                        "/" to TokenType.OPERATOR,
                        ">" to TokenType.OPERATOR,
                        "<" to TokenType.OPERATOR,
                    ),
            )

        private val VERSION_1_1 =
            VERSION_1_0 +
                TokenMapper(
                    words =
                        mapOf(
                            "const" to TokenType.KEYWORD,
                            "if" to TokenType.CONDITIONAL,
                            "else" to TokenType.CONDITIONAL,
                            "readInput" to TokenType.FUNCTION,
                            "readEnv" to TokenType.FUNCTION,
                            "boolean" to TokenType.DATA_TYPE,
                            "true" to TokenType.BOOLEANLITERAL,
                            "false" to TokenType.BOOLEANLITERAL,
                        ),
                    symbols =
                        mapOf(
                            "{" to TokenType.PUNCTUATOR,
                            "}" to TokenType.PUNCTUATOR,
                        ),
                )
    }
}
