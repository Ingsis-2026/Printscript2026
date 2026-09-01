package rules

import linter.BrokenRule
import token.Token
import token.TokenType

class SnakeCaseRule(
    private var errorMessage: String = "The following identifier must be in snake case: ",
) : Rule {
    override fun applyRule(tokens: List<List<Token>>): List<BrokenRule> =
        tokens
            .flatten()
            .filter { isIdentifierType(it) && !isSnakeCase(it.value) }
            .map { BrokenRule(errorMessage + it.value, it.getPosition()) }

    private fun isSnakeCase(identifier: String): Boolean =
        identifier.isNotEmpty() &&
            identifier[0].isLowerCase() &&
            // Should not have consecutive underscores
            !identifier.contains("__") &&
            identifier.all { it.isLowerCase() || it == '_' }

    private fun isIdentifierType(token: Token) = token.getType() == TokenType.IDENTIFIER

    override fun getRuleName(): String = "SnakeCase"

    override fun getRuleDescription(): String = errorMessage
}
