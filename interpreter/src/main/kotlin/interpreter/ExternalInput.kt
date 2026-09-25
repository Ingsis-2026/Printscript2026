package interpreter

import ast.DataType

/**
 * Valor traído de afuera del programa por `readInput` o `readEnv`, todavía sin interpretar.
 */
data class ExternalInput(
    val text: String,
    val origin: String,
) {
    fun asType(dataType: DataType): Any =
        when (dataType) {
            DataType.STRING -> text
            DataType.NUMBER -> text.toIntOrNull() ?: text.toDoubleOrNull() ?: throw cannotInterpretAs(dataType)
            DataType.BOOLEAN ->
                when (text) {
                    "true" -> true
                    "false" -> false
                    else -> throw cannotInterpretAs(dataType)
                }
        }

    private fun cannotInterpretAs(dataType: DataType) =
        InterpreterException("$origin devolvió \"$text\", que no puede interpretarse como ${dataType.keyword}")
}
