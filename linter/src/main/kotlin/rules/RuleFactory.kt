package rules

import linter.LinterVersion

class RuleFactory {
    fun createRules(
        ruleNames: List<RuleName>,
        version: LinterVersion,
    ): List<Rule> = ruleNames.map { createRule(it, version) }

    /**
     * Construye la regla, o la rechaza si es más nueva que la versión que se está analizando.
     *
     * El `when` es exhaustivo sobre [RuleName], así que agregar una regla deja de compilar acá
     * hasta que se diga cómo se construye.
     */
    private fun createRule(
        ruleName: RuleName,
        version: LinterVersion,
    ): Rule {
        require(version >= ruleName.since) { "Rule not available for this version" }
        return when (ruleName) {
            RuleName.CAMEL_CASE -> IdentifierFormatRule(IdentifierFormat.CAMEL_CASE)
            RuleName.SNAKE_CASE -> IdentifierFormatRule(IdentifierFormat.SNAKE_CASE)
            RuleName.PRINT_ONLY -> CallArgumentRule("println", PRINTLN_MESSAGE)
            RuleName.INPUT_ONLY -> CallArgumentRule("readInput", READ_INPUT_MESSAGE)
        }
    }

    private companion object {
        const val PRINTLN_MESSAGE = "Println must not be called with an expression"
        const val READ_INPUT_MESSAGE = "ReadInputs must not be called with an expression"
    }
}
