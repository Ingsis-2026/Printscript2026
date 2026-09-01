package rules

import linter.BrokenRule
import token.Token

class InputOnlyRule(
    private var errorMessage: String = "ReadInputs must not be called with an expression",
) : Rule {
    override fun applyRule(tokens: List<List<Token>>): List<BrokenRule> {
        val brokenRules = mutableListOf<BrokenRule>()
        for (row in tokens) {
            if (!containsReadInput(row)) continue
            if (containsExpression(argumentsOf(row))) {
                brokenRules.add(BrokenRule(errorMessage, row[0].getPosition()))
            }
        }
        return brokenRules
    }

    fun containsReadInput(tokens: List<Token>): Boolean = tokens.any { isReadInputType(it) }

    /** El Tokenizer emite el nombre de la función en minúsculas: `readInput` -> `readinput`. */
    private fun isReadInputType(token: Token): Boolean = token.isFunctionNamed("readinput")

    fun containsExpression(tokens: List<Token>): Boolean = tokens.any { it.isExpressionToken() }

    /** Tokens que siguen a la llamada a readInput, es decir sus argumentos. */
    private fun argumentsOf(tokens: List<Token>): List<Token> {
        val readInputPosition = tokens.indexOfFirst { isReadInputType(it) }
        if (readInputPosition < 0) return emptyList()
        return tokens.subList(readInputPosition + 1, tokens.size)
    }

    override fun getRuleName(): String = "InputOnly"

    override fun getRuleDescription(): String = errorMessage
}
