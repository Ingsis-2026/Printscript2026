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

    private fun isSnakeCase(identifier: String): Boolean =
        identifier.isNotEmpty() &&
            identifier[0].isLowerCase() &&
            // Should start with lowercase
            !identifier.contains("__") &&
            // Should not have consecutive underscores
            identifier.all { it.isLowerCase() || it == '_' }

    private fun isIdentifierType(token: Token) = token.getType() == TokenType.IDENTIFIER

    override fun getRuleName(): String = "SnakeCase"

    override fun getRuleDescription(): String = errorMessage
}
