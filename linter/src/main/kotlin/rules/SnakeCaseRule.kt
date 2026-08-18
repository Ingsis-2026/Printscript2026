package rules

import linter.BrokenRule
import token.Token
import token.TokenType

class SnakeCaseRule(
    private var errorMessage: String = "The following identifier must be in snake case: ",
) : Rule {
    private val brokenRules = mutableListOf<BrokenRule>()

    override fun applyRule(tokens: List<List<Token>>): List<BrokenRule> {
        tokens.flatten().forEach { token ->
            if (isIdentifierType(token) && !isSnakeCase(token.value)) {
                brokenRules.add(BrokenRule(errorMessage + token.value, token.getPosition()))
                println("Violation Found: ${token.value} at ${token.getPosition()}")
            }
        }
        return brokenRules
    }

    private fun isSnakeCase(identifier: String): Boolean {
        if (identifier.isEmpty()) return false
        if (!identifier[0].isLowerCase()) return false // Should start with lowercase
        if (identifier.contains("__")) return false // Should not have consecutive underscores

        return identifier.all { it.isLowerCase() || it == '_' }
    }

    private fun isIdentifierType(token: Token) = token.getType() == TokenType.IDENTIFIER

    override fun getRuleName(): String {
        return "SnakeCase"
    }

    override fun getRuleDescription(): String {
        return errorMessage
    }
}
