package lexer

import token.Token
import token.TokenPosition
import token.TokenType
import java.util.regex.Matcher

class Lexer(
    private val classifier: TokenMapper,
) {
    private val patternMatcher = PatternMatcher(classifier.getStrategyMap())

    fun execute(input: String): List<Token> {
        val tokens = mutableListOf<Token>()
        var row = 0
        input.lines().forEach { lineContent ->
            processLine(lineContent, row, tokens)
            row++
        }
        return tokens
    }

    private fun processLine(
        lineContent: String,
        row: Int,
        tokens: MutableList<Token>,
    ) {
        val matcher = createMatcher(lineContent)
        while (matcher.find()) {
            val tokenValue = matcher.group()
            val tokenType = classifier.classify(tokenValue)

            if (tokenType == TokenType.LITERAL) {
                val actualValue = extractTokenValue(tokenType, matcher)
                val startPos = TokenPosition(row, matcher.start() + 1)
                val endPos = TokenPosition(row, matcher.end() - 1)
                tokens.add(Token(tokenType, actualValue, startPos, endPos))
            }

            if (tokenType != TokenType.UNKNOWN) {
                val actualValue = extractTokenValue(tokenType, matcher)
                val startPos = TokenPosition(row, matcher.start())
                val endPos = TokenPosition(row, matcher.end())
                tokens.add(Token(tokenType, actualValue, startPos, endPos))
            } else {
                throw IllegalArgumentException("Carácter inválido encontrado: '$tokenValue'")
            }
        }
    }

    private fun createMatcher(lineContent: String): Matcher {
        val pattern = patternMatcher.createPattern()
        return pattern.matcher(lineContent)
    }

    private fun extractTokenValue(
        tokenType: TokenType,
        matcher: Matcher,
    ): String =
        when (tokenType) {
            TokenType.STRINGLITERAL -> {
                matcher.group().substring(1, matcher.group().length - 1)
            }
            else -> matcher.group()
        }
}
