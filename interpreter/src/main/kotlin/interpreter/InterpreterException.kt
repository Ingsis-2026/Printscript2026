package interpreter

/**
 * Error de ejecución producido al interpretar un programa PrintScript.
 *
 * Extiende [RuntimeException] para no alterar el comportamiento previo, pero
 * permite distinguir los fallos propios del intérprete de los del runtime.
 */
class InterpreterException(
    message: String,
) : RuntimeException(message)
