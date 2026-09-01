package linter

import token.TokenPosition

class BrokenRule(
    val ruleDescription: String,
    val errorPosition: TokenPosition,
) {
    override fun toString(): String = "Broken rule: $ruleDescription at $errorPosition"
}
