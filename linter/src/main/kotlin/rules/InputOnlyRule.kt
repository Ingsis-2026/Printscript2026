package rules

import linter.BrokenRule
import token.Token
import token.TokenType

class InputOnlyRule(
    private var errorMessage: String = "ReadInputs must not be called with an expression",
) : Rule {
    private val brokenRules = mutableListOf<BrokenRule>()

    override fun applyRule(tokens: List<List<Token>>): List<BrokenRule> {
        for (row in tokens) {
            if (containsReadInput(row)) {
                if (containsExpression(splitTokens(row))) {
                    brokenRules.add(BrokenRule(errorMessage, row[0].getPosition()))
                }
            }
        }
        return brokenRules
    }

    fun containsReadInput(tokens: List<Token>): Boolean {
        for (token in tokens) {
            if (isReadInputType(token)) {
                return true
            }
        }
        return false
    }

    private fun isReadInputType(token: Token): Boolean = token.isFunctionNamed("inputonly")

    fun containsExpression(tokens: List<Token>): Boolean {
        for (token in tokens) {
            if (isExpressionType(token)) {
                return true
            }
        }
        return false
    }

    private fun isExpressionType(token: Token): Boolean =
        token.getType() != TokenType.IDENTIFIER &&
            token.getType() != TokenType.PUNCTUATOR &&
            token.getType() != TokenType.LITERAL

    private fun splitTokens(tokens: List<Token>): List<Token> {
        var readInputPosition = 0
        for (token in tokens) {
            if (isReadInputType(token)) {
                readInputPosition = tokens.indexOf(token)
                break
            }
        }
        return tokens.subList(readInputPosition + 1, tokens.size)
    }

    override fun getRuleName(): String = "InputOnly"

    override fun getRuleDescription(): String = errorMessage
}
