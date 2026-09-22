package rules

import linter.LinterVersion

class RuleFactory {
    fun createRules(
        ruleNames: List<String>,
        version: LinterVersion,
    ): List<Rule> = ruleNames.map { createRule(it, version) }

    private fun createRule(
        ruleName: String,
        version: LinterVersion,
    ): Rule =
        when (ruleName.lowercase()) {
            "camelcase" -> IdentifierFormatRule(IdentifierFormat.CAMEL_CASE)
            "snakecase" -> IdentifierFormatRule(IdentifierFormat.SNAKE_CASE)
            "printonly" -> CallArgumentRule("println", PRINTLN_MESSAGE)
            "inputonly" -> requireAtLeast(LinterVersion.VERSION_1_1, version) { CallArgumentRule("readInput", READ_INPUT_MESSAGE) }
            else -> throw IllegalArgumentException("Rule not available for this version")
        }

    private fun requireAtLeast(
        minimum: LinterVersion,
        version: LinterVersion,
        rule: () -> Rule,
    ): Rule {
        if (version < minimum) {
            throw IllegalArgumentException("Rule not available for this version")
        }
        return rule()
    }

    private companion object {
        const val PRINTLN_MESSAGE = "Println must not be called with an expression"
        const val READ_INPUT_MESSAGE = "ReadInputs must not be called with an expression"
    }
}
