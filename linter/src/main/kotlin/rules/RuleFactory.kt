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
            "camelcase" -> CamelCaseRule()
            "snakecase" -> SnakeCaseRule()
            "printonly" -> PrintOnlyRule()
            "inputonly" -> requireAtLeast(LinterVersion.VERSION_1_1, version) { InputOnlyRule() }
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
}
