package interpreter

/**
 * Valor traído de afuera del programa por `readInput` o `readEnv`, todavía sin interpretar.
 *
 * Qué tipo tiene lo decide su destino —la variable a la que se asigna, o `string` cuando es
 * argumento de `println`—, así que el evaluador de la función no puede resolverlo por su
 * cuenta: adivinar el tipo a partir del texto, como se hacía antes, dejaba un `5` tipeado en
 * una variable `string` convertido en número. El texto viaja crudo hasta quien conoce el tipo
 * esperado y recién ahí se convierte con [asType].
 */
data class ExternalInput(
    val text: String,
    val origin: String,
) {
    fun asType(dataType: String): Any =
        when (dataType) {
            "string" -> text
            "number" -> text.toIntOrNull() ?: text.toDoubleOrNull() ?: throw cannotInterpretAs(dataType)
            "boolean" ->
                when (text) {
                    "true" -> true
                    "false" -> false
                    else -> throw cannotInterpretAs(dataType)
                }
            else -> throw InterpreterException("$origin no puede devolver un valor de tipo $dataType")
        }

    private fun cannotInterpretAs(dataType: String) =
        InterpreterException("$origin devolvió \"$text\", que no puede interpretarse como $dataType")
}
