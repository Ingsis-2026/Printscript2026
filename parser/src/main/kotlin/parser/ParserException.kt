package parser

/**
 * Error producido al construir el AST a partir de los tokens.
 *
 * Extiende [RuntimeException] para no alterar el comportamiento previo, pero
 * permite distinguir los fallos propios del parser de los del runtime.
 */
class ParserException(
    message: String,
) : RuntimeException(message)
