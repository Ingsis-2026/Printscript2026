package lexer

import token.Token
import token.TokenPosition
import token.TokenType
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Convierte código fuente en [Token]s, una línea por vez.
 *
 * Las definiciones de la versión se unen en un solo regex, con un grupo por definición. Cada match
 * es un lexema: dónde empieza y termina es la posición del token, y el grupo que lo capturó dice de
 * qué tipo es. Ante dos definiciones que reconocen el mismo texto gana la primera de la lista.
 */
class Lexer(
    tokenMapper: TokenMapper,
) {
    private val definitions: List<TokenDefinition> = tokenMapper.definitions

    private val anyTokenPattern: Pattern =
        Pattern.compile(
            definitions.withIndex().joinToString("|") { (index, definition) -> "(?<${groupOf(index)}>${definition.regex.pattern})" },
        )

    fun execute(input: String): List<Token> = convertToTokens(input.lineSequence()).toList()

    fun convertToTokens(lines: Sequence<String>): Sequence<Token> = lines.mapIndexed(::tokenizeLine).flatten()

    fun convertToTokens(inputStream: InputStream): Sequence<Token> =
        convertToTokens(BufferedReader(InputStreamReader(inputStream)).lineSequence())

    private fun tokenizeLine(
        row: Int,
        lineContent: String,
    ): List<Token> {
        val tokens = mutableListOf<Token>()
        val matcher = anyTokenPattern.matcher(lineContent)
        var scannedUpTo = 0

        while (matcher.find()) {
            rejectSkippedText(lineContent, row, from = scannedUpTo, to = matcher.start())
            tokens.add(tokenFoundBy(matcher, row))
            scannedUpTo = matcher.end()
        }
        rejectSkippedText(lineContent, row, from = scannedUpTo, to = lineContent.length)

        return tokens
    }

    /**
     * Rechaza el texto que el patrón no reconoció.
     *
     * `Matcher.find` no falla ante un lexema inválido: simplemente lo saltea y sigue con el
     * match siguiente. Para detectarlo hay que mirar los huecos que deja entre matches —y la
     * cola de la línea—, donde cualquier cosa que no sea espacio en blanco es un error.
     */
    private fun rejectSkippedText(
        lineContent: String,
        row: Int,
        from: Int,
        to: Int,
    ) {
        if (from >= to) return
        val skipped = lineContent.substring(from, to)
        if (skipped.isBlank()) return

        val leadingBlanks = skipped.indexOfFirst { !it.isWhitespace() }
        val invalid = skipped.trim()
        val startColumn = from + leadingBlanks
        throw LexerException(
            "Carácter inválido encontrado: '$invalid'",
            TokenPosition(row, startColumn),
            TokenPosition(row, startColumn + invalid.length),
        )
    }

    private fun tokenFoundBy(
        matcher: Matcher,
        row: Int,
    ): Token {
        val type = typeCapturedBy(matcher)
        return Token(type, valueOf(type, matcher.group()), TokenPosition(row, matcher.start()), TokenPosition(row, matcher.end()))
    }

    /** `start` es -1 para los grupos que no participaron del match. */
    private fun typeCapturedBy(matcher: Matcher): TokenType {
        val capturing = definitions.indices.first { index -> matcher.start(groupOf(index)) != -1 }
        return definitions[capturing].type
    }

    /** El valor que el token lleva de verdad: un string literal pierde las comillas que lo delimitan. */
    private fun valueOf(
        tokenType: TokenType,
        lexeme: String,
    ): String =
        when (tokenType) {
            TokenType.STRINGLITERAL -> lexeme.removeSurrounding("\"", "\"").removeSurrounding("'", "'")
            else -> lexeme
        }

    /**
     * Cada definición se busca por nombre y no por número de grupo, porque su regex puede tener
     * grupos propios (el de los decimales de NUMBERLITERAL) que correrían la numeración.
     */
    private fun groupOf(definitionIndex: Int): String = "definition$definitionIndex"
}
