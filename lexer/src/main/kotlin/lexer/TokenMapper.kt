package lexer

import token.TokenType
import java.util.regex.Pattern

/**
 * Las reglas del lenguaje para una versión: con qué regex se reconoce cada tipo de token, qué
 * palabras son reservadas, y qué palabras existen en el lenguaje pero no en esta versión.
 *
 * El [Lexer] lo consulta en dos momentos: [combinedPattern] para encontrar dónde están los
 * lexemas, y [resolve] para decidir qué es cada uno.
 */
class TokenMapper(
    private val strategyMap: Map<TokenType, RegexTokenClassifier>,
    private val reservedKeywords: Map<String, TokenType> = defaultReservedKeywords(),
    private val disallowedKeywords: Map<String, String> = emptyMap(),
) {
    constructor(version: String) : this(
        strategyMap = getStrategiesForVersion(version),
        disallowedKeywords = getDisallowedForVersion(version),
    )

    /**
     * Los regex de todos los tipos unidos en uno solo con `|`, para que el [Lexer] pueda recorrer
     * una línea encontrando lexemas de cualquier tipo en una sola pasada.
     *
     * El orden del mapa es el orden de las alternativas, y ante dos regex que matchean en la misma
     * posición gana el primero: por eso `\blet\b` (KEYWORD) va antes que el regex de IDENTIFIER,
     * que también matchearía "let".
     */
    fun combinedPattern(): Pattern {
        val alternatives = strategyMap.values.joinToString("|") { classifier -> "(${classifier.regex.pattern})" }
        return Pattern.compile(alternatives)
    }

    /**
     * Qué token es [input] en esta versión, o por qué esta versión lo rechaza.
     *
     * Concentra el orden en que hay que decidirlo: primero si es una palabra prohibida (válida en
     * el lenguaje pero no en esta versión, como "const" en 1.0), porque ese caso merece su propio
     * motivo, y recién después si matchea algún tipo conocido. Quien sabe dónde apareció el lexema
     * es el [Lexer], así que es él quien convierte un [TokenResolution.Rejected] en un error
     * ubicado en el fuente.
     */
    fun resolve(input: String): TokenResolution {
        disallowedReason(input)?.let { reason -> return TokenResolution.Rejected(reason) }

        val type = classify(input)
        return if (type == TokenType.UNKNOWN) {
            TokenResolution.Rejected("Carácter inválido encontrado: '$input'")
        } else {
            TokenResolution.Recognized(type)
        }
    }

    /** Motivo por el que la versión no admite este lexema, o `null` si lo admite. */
    fun disallowedReason(input: String): String? = disallowedKeywords[input]

    fun classify(input: String): TokenType {
        if (input.isBlank()) return TokenType.UNKNOWN

        return reservedKeywords[input]
            ?: strategyMap.entries.firstOrNull { it.value.classify(input) }?.key
            ?: TokenType.UNKNOWN
    }

    fun getStrategyMap(): Map<TokenType, RegexTokenClassifier> = strategyMap

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

        fun version10Strategies(): Map<TokenType, RegexTokenClassifier> =
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

        fun version11Strategies(): Map<TokenType, RegexTokenClassifier> {
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

        private fun getStrategiesForVersion(version: String): Map<TokenType, RegexTokenClassifier> =
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
