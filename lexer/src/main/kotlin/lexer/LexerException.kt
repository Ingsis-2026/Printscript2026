package lexer

import diagnostics.PrintScriptException
import token.TokenPosition

/** Error de análisis léxico, con la ubicación exacta del lexema inválido. */
class LexerException(
    message: String,
    startPosition: TokenPosition? = null,
    endPosition: TokenPosition? = null,
) : PrintScriptException(message, startPosition, endPosition)
