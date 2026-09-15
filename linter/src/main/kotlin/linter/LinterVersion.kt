package linter

/**
 * Versiones del lenguaje que el linter reconoce.
 *
 * Se declaran de menor a mayor: [rules.RuleFactory] compara por ese orden para saber desde qué
 * versión está disponible una regla, así que una versión nueva se agrega al final.
 */
enum class LinterVersion(
    val version: String,
) {
    VERSION_1_0("1.0"),
    VERSION_1_1("1.1"),
    ;

    companion object {
        fun fromString(version: String): LinterVersion? = entries.find { it.version == version }
    }
}
