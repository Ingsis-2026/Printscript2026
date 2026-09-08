package formatter

import lexer.Lexer
import token.Token
import token.TokenType

/**
 * Un token junto con el texto exacto que ocupaba en el fuente.
 *
 * El texto se guarda porque el [Token] no alcanza para reproducir el original: el Lexer le
 * quita las comillas a los literales de texto, y el formatter tiene que devolverlas tal cual
 * estaban.
 */
internal data class SourceToken(
    private val token: Token,
    val text: String,
) {
    val value: String get() = token.value
    val type: TokenType get() = token.getType()
    val startRow: Int get() = token.getPosition().row
    val startColumn: Int get() = token.getPosition().column
    val endRow: Int get() = token.getFinalPosition().row
    val endColumn: Int get() = token.getFinalPosition().column

    val isColon: Boolean get() = type == TokenType.DECLARATOR
    val isAssignation: Boolean get() = type == TokenType.ASSIGNATION
    val isOperator: Boolean get() = type == TokenType.OPERATOR
    val isSemicolon: Boolean get() = value == ";"
    val isOpeningBrace: Boolean get() = value == "{"
    val isClosingBrace: Boolean get() = value == "}"
}

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
            var currentLine = ""
            val recorded = lines.map { line -> line.also { currentLine = it } }

            for (token in lexer.convertToTokens(recorded)) {
                val start = token.getPosition().column
                val end = token.getFinalPosition().column
                yield(SourceToken(token, currentLine.substring(start, end)))
            }
        }
}
