package rules

import linter.BrokenRule
import token.Token
import token.TokenType

class CamelCaseRule(
    private var errorMessage: String = "The following identifier must be in camel case",
) : Rule {
    private val brokenRules = mutableListOf<BrokenRule>()

    override fun applyRule(tokens: List<List<Token>>): List<BrokenRule> {
        for (row in tokens) {
            for (token in row) {
                if (isIdentifierType()(token) && !isCamelCase(token)) {
                    brokenRules.add(BrokenRule(errorMessage, token.getPosition()))
                    println("Violation Found: ${token.value} at ${token.getPosition()}")
                }
            }
        }
        return brokenRules
    }

    private fun isIdentifierType() = { token: Token -> token.getType() == TokenType.IDENTIFIER }

    private fun isCamelCase(token: Token): Boolean {
        val segment = token.value.split("_")
        return segment.size <= 1
    }

    override fun getRuleName(): String {
        return "CamelCase"
    }

    override fun getRuleDescription(): String {
        return errorMessage
    }
}
