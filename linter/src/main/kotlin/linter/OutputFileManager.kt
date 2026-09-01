package linter

import java.io.File

class OutputFileManager {
    fun saveToFile(
        content: String,
        filePath: String,
    ) {
        val file = File(filePath)
        file.writeText(content)
    }

    fun createTxtReport(brokenRules: List<BrokenRule>): String {
        val report = StringBuilder()
        for (brokenRule in brokenRules) {
            report.append("${brokenRule.ruleDescription} in line: ${brokenRule.errorPosition.row}\n")
        }
        return report.toString()
    }

    fun createHtmlReport(brokenRules: List<BrokenRule>): String {
        val report = StringBuilder()
        report.append("<html><head><title>Broken Rules Report</title></head><body><h1>Broken Rules</h1><ul>")
        for (brokenRule in brokenRules) {
            report.append("<li>${brokenRule.ruleDescription} in line: ${brokenRule.errorPosition.row}</li>")
        }
        report.append("</ul></body></html>")
        return report.toString()
    }
}
