package rules

import version.Version

/**
 * Las reglas que el linter sabe construir, una por ajuste del archivo de configuración.
 *
 * [since] es la versión del lenguaje en la que la regla apareció. Es un dato de la regla y no de
 * quien la construye: `readInput` no existe en 1.0, así que pedirla para un fuente 1.0 es un
 * error de configuración, y una versión posterior a la que la introdujo la sigue admitiendo.
 */
enum class RuleName(
    val since: Version,
) {
    CAMEL_CASE(Version.V1_0),
    SNAKE_CASE(Version.V1_0),
    PRINT_ONLY(Version.V1_0),
    INPUT_ONLY(Version.V1_1),
}
