package linter

import ast.ASTNode
import ast.Tokenizer
import rules.Rule
import rules.RuleFactory
import rules.RuleValidator

class Linter(private var version: LinterVersion) {
    private var rules: List<Rule> = listOf()
    private val jsonReader = RuleJsonReader()
    private val ruleFactory = RuleFactory()
    private val tokenizer = Tokenizer()
    private val validator = RuleValidator()
    private val fileManager = OutputFileManager()

    fun readJson(jsonContent: String) {
        val jsonRules = jsonReader.getRulesFromJson(jsonContent)
        rules = ruleFactory.createRules(jsonRules, version)
    }

    fun check(trees: List<ASTNode>): LinterOutput {
        val tokens = tokenizer.parseToTokens(trees)
        val brokenRules = validator.checkRule(rules, tokens)
        val linterOutput = LinterOutput()
        if (brokenRules.isNotEmpty()) {
            for (brokenRule in brokenRules) {
                linterOutput.addBrokenRule(brokenRule)
            }
        }
        return linterOutput
    }

    fun writeToFile(
        content: String,
        filePath: String,
    ) {
        fileManager.saveToFile(content, filePath)
    }

    fun createTxtContent(brokenRules: List<BrokenRule>): String {
        return fileManager.createTxtReport(brokenRules)
    }

    fun createHtmlContent(brokenRules: List<BrokenRule>): String {
        return fileManager.createHtmlReport(brokenRules)
    }

    fun getRules(): List<Rule> {
        return rules
    }
}
