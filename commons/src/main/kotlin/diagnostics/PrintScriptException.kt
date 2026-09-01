package diagnostics

import token.TokenPosition

/**
 * Error de PrintScript acompañado de su ubicación en el archivo fuente.
 *
 * La consigna pide informar fila y columna de inicio **y de fin** del problema. La posición
 * viaja como dato en la excepción en lugar de quedar embebida en el texto, de modo que
 * [message] se mantiene limpio para los tests y la CLI arma la salida con [describe].
 *
 * Las posiciones son opcionales: hay errores que se detectan sin un token a mano.
 */
abstract class PrintScriptException(
    message: String,
    val startPosition: TokenPosition? = null,
    val endPosition: TokenPosition? = null,
) : RuntimeException(message) {
    /** Mensaje con la ubicación en coordenadas 1-based, para mostrar al usuario. */
    fun describe(): String {
        val text = message ?: ""
        val location = locationText() ?: return text
        return "$text ($location)"
    }

    private fun locationText(): String? {
        val start = startPosition ?: return null
        val end = endPosition ?: start
        if (start == end) return "línea ${start.row + 1}, columna ${start.column + 1}"
        return "desde línea ${start.row + 1}, columna ${start.column + 1} " +
            "hasta línea ${end.row + 1}, columna ${end.column + 1}"
    }
}
