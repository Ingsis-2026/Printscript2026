package rules

import linter.BrokenRule
import token.Token

class PrintOnlyRule(
    private var errorMessage: String = "Println must not be called with an expression",
) : Rule {
    override fun applyRule(tokens: List<List<Token>>): List<BrokenRule> {
        val brokenRules = mutableListOf<BrokenRule>()
        for (row in tokens) {
            if (!containsPrintln(row)) continue
            if (containsExpression(row.drop(1))) {
                brokenRules.add(BrokenRule(errorMessage, row[0].getPosition()))
            }
        }
        return brokenRules
    }

    fun containsPrintln(tokens: List<Token>): Boolean = tokens.firstOrNull()?.isFunctionNamed("println") ?: false

    fun containsExpression(tokens: List<Token>): Boolean = tokens.any { it.isExpressionToken() }

    override fun getRuleName(): String = "PrintOnly"

    override fun getRuleDescription(): String = errorMessage
}
