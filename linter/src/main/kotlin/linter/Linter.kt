package linter

import ast.ASTNode
import rules.Rule
import rules.RuleFactory
import rules.statementsOf
import version.Version

class Linter(
    private val version: Version,
) {
    /** Para el adaptador del TCK, que construye el linter con un [LinterVersion]. */
    constructor(version: LinterVersion) : this(version.version)

    private var rules: List<Rule> = listOf()
    private val jsonReader = RuleJsonReader()
    private val ruleFactory = RuleFactory()

    /**
     * Configura el linter con las reglas del archivo.
     *
     * Hasta que se la llame, el linter no tiene reglas y [check] no encuentra nada, así que
     * quien construya uno acá adentro conviene que use [forConfig] y no pase por ese estado
     * intermedio. Este camino existe porque el adaptador del TCK construye el linter primero y
     * le pasa la configuración después.
     */
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

    companion object {
        /** Un linter que ya tiene sus reglas: no existe el momento en que no revisa nada. */
        fun forConfig(
            version: Version,
            jsonContent: String,
        ): Linter = Linter(version).apply { readJson(jsonContent) }
    }
}
