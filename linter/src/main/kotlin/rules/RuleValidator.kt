package rules

import linter.BrokenRule
import token.Token

class RuleValidator {
    fun checkRule(
        rules: List<Rule>,
        tokens: List<List<Token>>,
    ): List<BrokenRule> {
        val brokenRules = mutableListOf<BrokenRule>()
        for (rule in rules) {
            println("Checking rule: ${rule.getRuleName()}") // Debugging output
            val violations = rule.applyRule(tokens)
            println("Violations found: $violations") // Debugging output
            brokenRules.addAll(violations)
        }
        return brokenRules
    }
}
