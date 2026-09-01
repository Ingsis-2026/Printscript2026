package rules

import linter.BrokenRule
import token.Token

class RuleValidator {
    fun checkRule(
        rules: List<Rule>,
        tokens: List<List<Token>>,
    ): List<BrokenRule> = rules.flatMap { it.applyRule(tokens) }
}
