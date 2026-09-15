package interpreter

/**
 * Valor traído de afuera del programa por `readInput` o `readEnv`, todavía sin interpretar.
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
