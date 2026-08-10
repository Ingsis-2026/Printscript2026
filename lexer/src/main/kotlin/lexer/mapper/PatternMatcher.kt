package lexer.mapper

import lexer.classifier.RegexTokenClassifier
import lexer.classifier.TokenClassifierStrategy
import token.TokenType
import java.util.regex.Pattern

class PatternMatcher(private val strategyMap: Map<TokenType, TokenClassifierStrategy>) {
    fun createPattern(): Pattern {
        val patternBuilder = StringBuilder()
        for ((_, strategy) in strategyMap) {
            if (strategy is RegexTokenClassifier) {
                patternBuilder.append("(${strategy.regex.pattern})|")
            }
        }
        return Pattern.compile(patternBuilder.toString().removeSuffix("|"))
    }
}
