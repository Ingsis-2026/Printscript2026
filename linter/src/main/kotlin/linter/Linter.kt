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

    fun check(trees: List<ASTNode>): LinterOutput {
        val tokens = tokenizer.parseToTokens(trees)
        val linterOutput = LinterOutput()
        for (brokenRule in validator.checkRule(rules, tokens)) {
            linterOutput.addBrokenRule(brokenRule)
        }
        return linterOutput
    }

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
