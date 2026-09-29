package formatter

import lexer.Lexer

/**
 * Empareja cada token con su texto original sin dejar de ser perezoso.
 *
 * El Lexer consume una línea, emite todos sus tokens y recién entonces pide la siguiente, así
 * que mientras se entregan los tokens de una fila la última línea leída es justamente la de
 * esos tokens. Alcanza con recordar esa única línea: no se retiene el fuente.
 */
internal class SourceScanner(
    private val lexer: Lexer,
) {
    fun scan(lines: Sequence<String>): Sequence<SourceToken> =
        sequence {
            var lastLineRead = ""
            val remembered = lines.onEach { lastLineRead = it }

            for (token in lexer.convertToTokens(remembered)) {
                val text = lastLineRead.substring(token.getPosition().column, token.getFinalPosition().column)
                yield(SourceToken(token, text))
            }
        }
}
