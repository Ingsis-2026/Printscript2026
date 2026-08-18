package rules

import linter.BrokenRule
import token.Token
import token.TokenType

class PrintOnlyRule(
    private var errorMessage: String = "Println must not be called with an expression",
) : Rule {
    private val brokenRules = mutableListOf<BrokenRule>()

    override fun applyRule(tokens: List<List<Token>>): List<BrokenRule> {
        for (row in tokens) {
            if (containsPrintln(row)) {
                val tokenSublist = row.subList(1, row.size)
                if (containsExpression(tokenSublist)) brokenRules.add(BrokenRule(errorMessage, row[0].getPosition()))
            }
        }
        return brokenRules
    }

    fun containsPrintln(tokens: List<Token>): Boolean {
        val firstToken = tokens[0]
        return isPrintlnType(firstToken)
    }

    private fun isPrintlnType(token: Token): Boolean {
        return token.getType() == TokenType.FUNCTION && token.value.lowercase() == "println"
    }

    fun containsExpression(tokens: List<Token>): Boolean {
        for (token in tokens) {
            if (isExpressionType(token)) {
                return true
            }
        }
        return false
    }

    private fun isExpressionType(token: Token): Boolean {
        return token.getType() != TokenType.IDENTIFIER &&
            token.getType() != TokenType.PUNCTUATOR &&
            token.getType() != TokenType.LITERAL
    }

    override fun getRuleName(): String {
        return "PrintOnly"
    }

    override fun getRuleDescription(): String {
        return errorMessage
    }
}
