package rules

import linter.LinterVersion

class RuleFactory() {
    fun createRules(
        jsonRules: List<Rule>,
        version: LinterVersion,
    ): List<Rule> {
        return when (version) {
            LinterVersion.VERSION_1_0 -> createVersion10(jsonRules)
            LinterVersion.VERSION_1_1 -> createVersion11(jsonRules)
        }
    }

    private fun createVersion10(jsonRules: List<Rule>): List<Rule> {
        val rules = mutableListOf<Rule>()
        for (rule in jsonRules) {
            when (rule.getRuleName().lowercase()) {
                "camelcase" -> rules.add(CamelCaseRule())
                "snakecase" -> rules.add(SnakeCaseRule())
                "printonly" -> rules.add(PrintOnlyRule())
                else -> {
                    throw IllegalArgumentException("Rule not available for this version")
                }
            }
        }
        return rules
    }

    private fun createVersion11(jsonRules: List<Rule>): List<Rule> {
        val rules = mutableListOf<Rule>()
        for (rule in jsonRules) {
            when (rule.getRuleName().lowercase()) {
                "camelcase" -> rules.add(CamelCaseRule())
                "snakecase" -> rules.add(SnakeCaseRule())
                "printonly" -> rules.add(PrintOnlyRule())
                "inputonly" -> rules.add(InputOnlyRule())
                else -> {
                    throw IllegalArgumentException("Rule not available for this version")
                }
            }
        }
        return rules
    }
}
