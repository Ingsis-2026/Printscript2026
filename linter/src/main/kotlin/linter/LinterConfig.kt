package linter

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * El archivo de configuración del linter, tal como lo escribe quien lo usa.
 *
 * Cada propiedad lleva el nombre de la clave que la trae, para que el archivo y esta clase se
 * puedan leer uno al lado del otro. `ignoreUnknown` deja pasar las claves que no gobiernan
 * ninguna regla de esta versión.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
class LinterConfig {
    @JsonProperty("identifier_format")
    var identifierFormat: String? = null

    @JsonProperty("mandatory-variable-or-literal-in-println")
    var mandatoryVariableOrLiteralInPrintln: Boolean = false

    @JsonProperty("mandatory-variable-or-literal-in-readInput")
    var mandatoryVariableOrLiteralInReadInput: Boolean = false
}
