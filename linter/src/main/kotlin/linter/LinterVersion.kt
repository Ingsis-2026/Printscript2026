package linter

import version.Version

/**
 * La versión como la recibe el adaptador del TCK, que construye el linter con este tipo.
 *
 * Dentro del proyecto la versión es un [Version]; este enum es sólo la puerta de entrada que el
 * TCK ya conoce, y cada valor dice a qué [Version] corresponde.
 */
enum class LinterVersion(
    val version: Version,
) {
    VERSION_1_0(Version.V1_0),
    VERSION_1_1(Version.V1_1),
    ;

    companion object {
        fun fromString(version: String): LinterVersion? = entries.find { it.version.number == version }
    }
}
