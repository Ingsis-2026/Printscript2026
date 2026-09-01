package parser

import diagnostics.PrintScriptException
import token.TokenPosition

/**
 * Error producido al construir el AST a partir de los tokens.
 *
 * Extiende [PrintScriptException] para transportar la ubicación del problema (fila y columna
 * de inicio y de fin), que es lo que la CLI necesita para reportarlo.
 */
class ParserException(
    message: String,
    startPosition: TokenPosition? = null,
    endPosition: TokenPosition? = null,
) : PrintScriptException(message, startPosition, endPosition)
