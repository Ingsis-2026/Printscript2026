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
            "inputonly" -> requireVersion11(version) { InputOnlyRule() }
            else -> throw IllegalArgumentException("Rule not available for this version")
        }

    private fun requireVersion11(
        version: LinterVersion,
        rule: () -> Rule,
    ): Rule {
        if (version != LinterVersion.VERSION_1_1) {
            throw IllegalArgumentException("Rule not available for this version")
        }
        return rule()
    }
}
