package linter

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

/**
 * Lee la configuración del linter y la traduce a nombres de regla.
 *
 * Devuelve nombres —no instancias— porque el único constructor de reglas es
 * [rules.RuleFactory], que además aplica el filtro por versión.
 */
class RuleJsonReader {
    fun getRuleNamesFromJson(jsonContent: String): List<String> {
        val config = jacksonObjectMapper().readValue(jsonContent, LinterConfig::class.java)
        val ruleNames = mutableListOf<String>()

        when (config.identifierFormat?.replace(" ", "")?.lowercase()) {
            "camelcase" -> ruleNames.add("camelcase")
            "snakecase" -> ruleNames.add("snakecase")
        }

        if (config.mandatoryVariableOrLiteralInPrintln) ruleNames.add("printonly")
        if (config.mandatoryVariableOrLiteralInReadInput) ruleNames.add("inputonly")

        return ruleNames
    }
}
