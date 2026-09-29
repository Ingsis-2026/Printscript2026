package formatter

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
    private val type: TokenType get() = token.getType()

    val row: Int get() = token.getPosition().row
    val startColumn: Int get() = token.getPosition().column
    val endColumn: Int get() = token.getFinalPosition().column

    val isColon: Boolean get() = type == TokenType.DECLARATOR
    val isAssignation: Boolean get() = type == TokenType.ASSIGNATION
    val isOperator: Boolean get() = type == TokenType.OPERATOR
    val isDeclarationKeyword: Boolean get() = type == TokenType.KEYWORD
    val isPrintln: Boolean get() = type == TokenType.FUNCTION && token.value == "println"

    // La puntuación se reconoce por su tipo además de su texto: el string de `println(";")` llega
    // sin comillas, así que su valor es ";" igual que el del punto y coma que cierra la sentencia.
    val isSemicolon: Boolean get() = isPunctuator(";")
    val isOpeningBrace: Boolean get() = isPunctuator("{")
    val isClosingBrace: Boolean get() = isPunctuator("}")

    private fun isPunctuator(symbol: String): Boolean = type == TokenType.PUNCTUATOR && token.value == symbol
}
