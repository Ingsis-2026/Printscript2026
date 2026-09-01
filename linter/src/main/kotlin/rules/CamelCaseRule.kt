package rules

import linter.BrokenRule
import token.Token
import token.TokenType

class CamelCaseRule(
    private var errorMessage: String = "The following identifier must be in camel case",
) : Rule {
    override fun applyRule(tokens: List<List<Token>>): List<BrokenRule> =
        tokens
            .flatten()
            .filter { isIdentifierType(it) && !isCamelCase(it.value) }
            .map { BrokenRule(errorMessage, it.getPosition()) }

    private fun isIdentifierType(token: Token): Boolean = token.getType() == TokenType.IDENTIFIER

    /** camelCase: empieza en minúscula y no usa guiones bajos como separador. */
    private fun isCamelCase(identifier: String): Boolean =
        identifier.isNotEmpty() &&
            identifier[0].isLowerCase() &&
            !identifier.contains("_")

    override fun getRuleName(): String = "CamelCase"

    override fun getRuleDescription(): String = errorMessage
}
