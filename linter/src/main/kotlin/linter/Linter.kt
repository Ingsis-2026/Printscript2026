package linter

import ast.ASTNode
import ast.Tokenizer
import rules.Rule
import rules.RuleFactory
import rules.RuleValidator

class Linter(
    private var version: LinterVersion,
) {
    private var rules: List<Rule> = listOf()
    private val jsonReader = RuleJsonReader()
    private val ruleFactory = RuleFactory()
    private val tokenizer = Tokenizer()
    private val validator = RuleValidator()
    private val fileManager = OutputFileManager()

    fun readJson(jsonContent: String) {
        val ruleNames = jsonReader.getRuleNamesFromJson(jsonContent)
        rules = ruleFactory.createRules(ruleNames, version)
    }

    /**
     * Analiza el programa consumiendo el flujo de nodos sentencia por sentencia.
     *
     * Las reglas actuales evalúan cada sentencia de forma independiente, así que alcanza con
     * re-tokenizar una a la vez: nunca se retiene el AST completo. Lo único que crece es el
     * reporte, acotado por la cantidad de violaciones y no por el tamaño del fuente.
     */
    fun check(trees: Sequence<ASTNode>): LinterOutput {
        val linterOutput = LinterOutput()
        for (statementTokens in tokenizer.parseToTokens(trees)) {
            for (brokenRule in validator.checkRule(rules, listOf(statementTokens))) {
                linterOutput.addBrokenRule(brokenRule)
            }
        }
        return linterOutput
    }

    fun check(trees: List<ASTNode>): LinterOutput = check(trees.asSequence())

    fun writeToFile(
        content: String,
        filePath: String,
    ) {
        fileManager.saveToFile(content, filePath)
    }

    fun createTxtContent(brokenRules: List<BrokenRule>): String = fileManager.createTxtReport(brokenRules)

    fun createHtmlContent(brokenRules: List<BrokenRule>): String = fileManager.createHtmlReport(brokenRules)

    fun getRules(): List<Rule> = rules
}
