package linter

import token.TokenPosition

class LinterOutput {
    var isCorrect: Boolean = true
    var brokenRulesList: MutableList<String> = mutableListOf()

    fun addBrokenRule(brokenRule: BrokenRule) {
        isCorrect = false
        val ruleAsString = formatBrokenRule(brokenRule)
        brokenRulesList.add(ruleAsString)
    }

    private fun formatBrokenRule(brokenRule: BrokenRule): String {
        val position = brokenRule.errorPosition
        return "Broken rule: ${brokenRule.ruleDescription} at ${position.row}:${position.column}"
    }

    // new function for CLI, it basically generates the error message
    fun getBrokenRules(): List<BrokenRule> {
        return brokenRulesList.map { brokenRule ->
            val parts = brokenRule.split(" at ")
            val ruleDescription = parts[0].removePrefix("Broken rule: ")
            val positionParts = parts[1].split(":")
            val row = positionParts[0].toInt()
            val column = positionParts[1].toInt()
            BrokenRule(ruleDescription, TokenPosition(row, column))
        }
    }
}
