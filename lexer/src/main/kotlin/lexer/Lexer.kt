package lexer

import token.Token
import token.TokenPosition
import token.TokenType
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.regex.Pattern

class Lexer(
    private val classifier: TokenMapper,
) {
    private val pattern: Pattern = PatternMatcher(classifier.getStrategyMap()).createPattern()

    fun execute(input: String): List<Token> = convertToTokens(input)

    fun convertToTokens(input: String): List<Token> {
        val tokens = mutableListOf<Token>()
        var row = 0
        input.lineSequence().forEach { lineContent ->
            val lineTokens = convertLineToTokens(lineContent, row)
            tokens.addAll(lineTokens)
            row++
        }
        return tokens
    }

    fun convertToTokens(lines: Sequence<String>): Sequence<Token> =
        sequence {
            var row = 0
            for (lineContent in lines) {
                val lineTokens = convertLineToTokens(lineContent, row)
                for (token in lineTokens) {
                    yield(token)
                }
                row++
            }
        }

    fun convertToTokens(inputStream: InputStream): Sequence<Token> {
        val reader = BufferedReader(InputStreamReader(inputStream))
        return convertToTokens(reader.lineSequence())
    }

    private fun convertLineToTokens(
        lineContent: String,
        row: Int,
    ): List<Token> {
        val lineTokens = mutableListOf<Token>()
        val matcher = pattern.matcher(lineContent)
        var scanned = 0

        while (matcher.find()) {
            rejectSkippedText(lineContent, row, scanned, matcher.start())

            val rawValue = matcher.group()
            val tokenType = classifier.classify(rawValue)
            val startPos = TokenPosition(row, matcher.start())
            val endPos = TokenPosition(row, matcher.end())

            if (tokenType == TokenType.UNKNOWN) {
                throw LexerException("Carácter inválido encontrado: '$rawValue'", startPos, endPos)
            }

            val actualValue = extractTokenValue(tokenType, rawValue)
            lineTokens.add(Token(tokenType, actualValue, startPos, endPos))
            scanned = matcher.end()
        }
        rejectSkippedText(lineContent, row, scanned, lineContent.length)

        return lineTokens
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

    private fun extractTokenValue(
        tokenType: TokenType,
        rawValue: String,
    ): String =
        when (tokenType) {
            TokenType.STRINGLITERAL -> rawValue.removeSurrounding("\"", "\"").removeSurrounding("'", "'")
            else -> rawValue
        }
}
