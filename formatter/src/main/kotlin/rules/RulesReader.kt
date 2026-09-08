package rules

import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.error.YAMLException
import java.io.File
import java.io.InputStream

/**
 * Lee la configuración del formatter.
 *
 * Acepta YAML y JSON con el mismo parser porque JSON es un subconjunto de YAML, y no exige
 * ninguna clave: cada archivo de configuración habilita una sola regla y espera que el resto
 * quede sin tocar, así que una clave ausente es una regla apagada y no un error.
 */
class RulesReader {
    fun read(content: String): FormattingRules {
        if (content.isBlank()) return FormattingRules()

        val parsed =
            try {
                Yaml().load<Any?>(content)
            } catch (exception: YAMLException) {
                throw IllegalArgumentException("el archivo de reglas no es YAML ni JSON válido: ${exception.message}")
            } ?: return FormattingRules()

        val values =
            parsed as? Map<*, *>
                ?: throw IllegalArgumentException("el archivo de reglas debe ser un objeto con una regla por clave")

        return FormattingRules.from(values.entries.associate { (key, value) -> key.toString() to value })
    }

    fun read(stream: InputStream): FormattingRules = read(stream.readBytes().decodeToString())

    fun readFile(path: String): FormattingRules = read(File(path).readText())
}
