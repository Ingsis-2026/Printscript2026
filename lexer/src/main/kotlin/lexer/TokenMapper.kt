package lexer

import token.TokenType

class TokenMapper(
    private val strategyMap: Map<TokenType, TokenClassifierStrategy>,
    private val reservedKeywords: Map<String, TokenType> = defaultReservedKeywords(),
    private val disallowedKeywords: Map<String, String> = emptyMap(),
) {
    constructor(version: String) : this(
        strategyMap = getStrategiesForVersion(version),
        disallowedKeywords = getDisallowedForVersion(version),
    )

    /**
     * Motivo por el que la versión no admite este lexema, o `null` si lo admite.
     *
     * Se consulta en lugar de fallar dentro de [classify] porque quien conoce la ubicación del
     * lexema es el [Lexer]: así el rechazo puede informarse como un error ubicado en el fuente
     * y no como un error genérico sin fila ni columna.
     */
    fun disallowedReason(input: String): String? = disallowedKeywords[input]

    fun classify(input: String): TokenType {
        if (input.isBlank()) return TokenType.UNKNOWN

        return reservedKeywords[input]
            ?: strategyMap.entries.firstOrNull { it.value.classify(input) }?.key
            ?: TokenType.UNKNOWN
    }

    fun getStrategyMap(): Map<TokenType, TokenClassifierStrategy> = strategyMap

    companion object {
        fun defaultReservedKeywords(): Map<String, TokenType> =
            mapOf(
                "if" to TokenType.CONDITIONAL,
                "else" to TokenType.CONDITIONAL,
                "let" to TokenType.KEYWORD,
                "const" to TokenType.KEYWORD,
                "println" to TokenType.FUNCTION,
                "true" to TokenType.BOOLEANLITERAL,
                "false" to TokenType.BOOLEANLITERAL,
            )

        fun version10Strategies(): Map<TokenType, TokenClassifierStrategy> =
            mapOf(
                TokenType.KEYWORD to RegexTokenClassifier("""\blet\b""".toRegex()),
                TokenType.FUNCTION to RegexTokenClassifier("""\bprintln\b""".toRegex()),
                TokenType.PARENTHESIS to RegexTokenClassifier("""[()]""".toRegex()),
                TokenType.DECLARATOR to RegexTokenClassifier(""":""".toRegex()),
                TokenType.ASSIGNATION to RegexTokenClassifier("""=""".toRegex()),
                TokenType.DATA_TYPE to RegexTokenClassifier("""\bstring\b|\bnumber\b""".toRegex()),
                TokenType.OPERATOR to RegexTokenClassifier("""[-+*/%=><!&|^~]+""".toRegex()),
                TokenType.IDENTIFIER to RegexTokenClassifier("""\b[a-zA-Z_][a-zA-Z0-9_]*\b""".toRegex()),
                TokenType.STRINGLITERAL to RegexTokenClassifier("\'[^\']*\'|\"[^\"]*\"".toRegex()),
                TokenType.NUMBERLITERAL to RegexTokenClassifier("[0-9]+(\\.[0-9]+)?".toRegex()),
                TokenType.PUNCTUATOR to RegexTokenClassifier("""[()\[\],;.]""".toRegex()),
            )

        fun version11Strategies(): Map<TokenType, TokenClassifierStrategy> {
            val map = version10Strategies().toMutableMap()
            map[TokenType.CONDITIONAL] = RegexTokenClassifier("""\bif\b|\belse\b""".toRegex())
            map[TokenType.KEYWORD] = RegexTokenClassifier("""\blet\b|\bconst\b""".toRegex())
            map[TokenType.FUNCTION] = RegexTokenClassifier("println|readInput|readEnv".toRegex())
            map[TokenType.DATA_TYPE] = RegexTokenClassifier("(\\bstring\\b|\\bnumber\\b|\\bboolean\\b)".toRegex())
            map[TokenType.BOOLEANLITERAL] = RegexTokenClassifier("(true|false)".toRegex())
            map[TokenType.PUNCTUATOR] = RegexTokenClassifier("""[{}()\[\],;.]""".toRegex())
            return map
        }

        fun forVersion(version: String): TokenMapper = TokenMapper(version)

        private fun getStrategiesForVersion(version: String): Map<TokenType, TokenClassifierStrategy> =
            when (version) {
                "1.0" -> version10Strategies()
                "1.1" -> version11Strategies()
                else -> throw IllegalArgumentException("Unsupported version: $version")
            }

        private fun getDisallowedForVersion(version: String): Map<String, String> =
            when (version) {
                "1.0" -> mapOf("const" to "Const declarations are not allowed in version 1.0")
                "1.1" -> emptyMap()
                else -> throw IllegalArgumentException("Unsupported version: $version")
            }
    }
}
