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

        while (matcher.find()) {
            val rawValue = matcher.group()
            val tokenType = classifier.classify(rawValue)

            require(tokenType != TokenType.UNKNOWN) {
                "Carácter inválido encontrado: '$rawValue'"
            }

            val actualValue = extractTokenValue(tokenType, rawValue)
            val startPos = TokenPosition(row, matcher.start())
            val endPos = TokenPosition(row, matcher.end())
            lineTokens.add(Token(tokenType, actualValue, startPos, endPos))
        }

        return lineTokens
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
