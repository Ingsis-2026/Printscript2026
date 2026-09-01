package interpreter

import diagnostics.PrintScriptException
import token.TokenPosition

/**
 * Error de ejecución producido al interpretar un programa PrintScript.
 *
 * Extiende [PrintScriptException] para transportar la ubicación del nodo que falló. El AST
 * sólo guarda la posición de inicio de cada nodo, por lo que el fin suele quedar nulo.
 */
class InterpreterException(
    message: String,
    startPosition: TokenPosition? = null,
    endPosition: TokenPosition? = null,
) : PrintScriptException(message, startPosition, endPosition)
