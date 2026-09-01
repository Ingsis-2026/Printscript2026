package rules
import org.yaml.snakeyaml.Yaml
import java.io.File
import kotlin.reflect.KClass

class RulesReader(
    private val requiredRules: Map<String, KClass<*>>,
) {
    fun readFile(path: String): Map<String, Any> {
        val yaml: String = File(path).readText()
        val rulesMap: Map<String, Any> = Yaml().load(yaml)

        checkRules(rulesMap, requiredRules)

        return rulesMap
    }

    private fun checkRules(
        rulesMap: Map<String, Any>,
        requiredRules: Map<String, KClass<*>>,
    ) {
        for ((keyRequired, valueRequired) in requiredRules) {
            if (!rulesMap.containsKey(keyRequired)) {
                error("No se encuentra la regla $keyRequired en el archivo")
            }
            if (rulesMap[keyRequired] == null || !valueRequired.isInstance(rulesMap[keyRequired])) {
                error(
                    "El valor de la regla $keyRequired no es del tipo esperado",
                )
            }
            if (keyRequired == "lineBreakPrintln") {
                val value = rulesMap[keyRequired] as Int
                if (value < 0 || value > 2) {
                    error("El valor de la regla $keyRequired debe estar entre 0 y 2")
                }
            }
        }
    }
}
