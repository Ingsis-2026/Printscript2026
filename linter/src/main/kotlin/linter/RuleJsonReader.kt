package linter

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import rules.RuleName

/**
 * Lee la configuración del linter y la traduce a nombres de regla.
 *
 * Devuelve nombres —no instancias— porque el único constructor de reglas es
 * [rules.RuleFactory], que además aplica el filtro por versión. Los nombres son un [RuleName] y
 * no un texto: los únicos strings que quedan acá son los que escribe el usuario en el archivo.
 */
class RuleJsonReader {
    fun getRuleNamesFromJson(jsonContent: String): List<RuleName> {
        val config = jacksonObjectMapper().readValue(jsonContent, LinterConfig::class.java)
        val ruleNames = mutableListOf<RuleName>()

        when (config.identifierFormat?.replace(" ", "")?.lowercase()) {
            "camelcase" -> ruleNames.add(RuleName.CAMEL_CASE)
            "snakecase" -> ruleNames.add(RuleName.SNAKE_CASE)
        }

        if (config.mandatoryVariableOrLiteralInPrintln) ruleNames.add(RuleName.PRINT_ONLY)
        if (config.mandatoryVariableOrLiteralInReadInput) ruleNames.add(RuleName.INPUT_ONLY)

        return ruleNames
    }
}
