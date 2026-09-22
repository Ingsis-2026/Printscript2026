package linter

import ast.ASTNode
import rules.Rule
import rules.RuleFactory
import rules.statementsOf

class Linter(
    private var version: LinterVersion,
) {
    private var rules: List<Rule> = listOf()
    private val jsonReader = RuleJsonReader()
    private val ruleFactory = RuleFactory()

    fun readJson(jsonContent: String) {
        val ruleNames = jsonReader.getRuleNamesFromJson(jsonContent)
        rules = ruleFactory.createRules(ruleNames, version)
    }

    /**
     * Analiza el programa consumiendo el flujo de nodos sentencia por sentencia.
     *
     * Cada regla mira una sentencia por vez, así que nunca se retiene el AST completo. Lo único
     * que crece es el reporte, acotado por la cantidad de violaciones y no por el tamaño del
     * fuente.
     *
     * Los dos `for` anidados son el orden en que salen las violaciones: por sentencia, y dentro
     * de cada una por regla.
     */
    fun check(trees: Sequence<ASTNode>): LinterOutput {
        val linterOutput = LinterOutput()
        for (statement in trees.flatMap { statementsOf(it) }) {
            for (rule in rules) {
                for (brokenRule in rule.check(statement)) {
                    linterOutput.addBrokenRule(brokenRule)
                }
            }
        }
        return linterOutput
    }

    fun check(trees: List<ASTNode>): LinterOutput = check(trees.asSequence())

    fun getRules(): List<Rule> = rules
}
