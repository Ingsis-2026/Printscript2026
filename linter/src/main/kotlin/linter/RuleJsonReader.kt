package linter

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.File

@JsonIgnoreProperties(ignoreUnknown = true)
class FormattingRules {
    @JsonProperty("identifier_format")
    var identifier: String? = null

    @JsonProperty("enable_print_only")
    var isEnablePrintOnly: Boolean = false

    @JsonProperty("enable_input_only")
    var isEnableInputOnly: Boolean = false
}

/**
 * Lee la configuración del linter y la traduce a nombres de regla.
 *
 * Devuelve nombres —no instancias— porque el único constructor de reglas es
 * [rules.RuleFactory], que además aplica el filtro por versión.
 */
class RuleJsonReader {
    fun getRuleNamesFromFile(path: String): List<String> = getRuleNamesFromJson(File(path).readText())

    fun getRuleNamesFromJson(jsonContent: String): List<String> {
        val formattingRules = jacksonObjectMapper().readValue(jsonContent, FormattingRules::class.java)
        val ruleNames = mutableListOf<String>()

        when (formattingRules.identifier?.replace(" ", "")?.lowercase()) {
            "camelcase" -> ruleNames.add("camelcase")
            "snakecase" -> ruleNames.add("snakecase")
        }

        if (formattingRules.isEnablePrintOnly) ruleNames.add("printonly")
        if (formattingRules.isEnableInputOnly) ruleNames.add("inputonly")

        return ruleNames
    }
}
