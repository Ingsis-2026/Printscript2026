package lexer

import token.Token
import token.TokenPosition
import token.TokenType

/**
 * Convierte una línea en sus tokens, de izquierda a derecha.
 *
 * El primer carácter decide qué se lee: una letra empieza un nombre, un dígito un número, una
 * comilla un string, y cualquier otra cosa tiene que ser un símbolo de la versión.
 */
internal class LineScanner(
    private val vocabulary: TokenMapper,
    private val row: Int,
    private val line: String,
) {
    fun tokens(): List<Token> {
        val tokens = mutableListOf<Token>()
        var column = 0
        while (column < line.length) {
            if (line[column].isWhitespace()) {
                column++
            } else {
                val token = tokenAt(column)
                tokens.add(token)
                column = token.getFinalPosition().column
            }
        }
        return tokens
    }

    private fun tokenAt(start: Int): Token {
        val first = line[start]
        return when {
            first.startsName() -> nameAt(start)
            first.isNumeral() -> numberAt(start)
            first == '"' || first == '\'' -> stringAt(start)
            else -> symbolAt(start)
        }
    }

    /** Un nombre es una palabra de la versión si la versión la tiene, y un identificador si no. */
    private fun nameAt(start: Int): Token {
        val end = endOfRun(start) { it.continuesName() }
        val name = line.substring(start, end)
        return token(vocabulary.words[name] ?: TokenType.IDENTIFIER, name, start, end)
    }

    private fun numberAt(start: Int): Token {
        val integerEnd = endOfRun(start) { it.isNumeral() }
        val hasDecimals = line.getOrNull(integerEnd) == '.' && line.getOrNull(integerEnd + 1)?.isNumeral() == true
        val end = if (hasDecimals) endOfRun(integerEnd + 1) { it.isNumeral() } else integerEnd

        if (line.getOrNull(end)?.continuesName() == true) {
            val runEnd = endOfRun(end) { it.continuesName() }
            throw LexerException("Número inválido: '${line.substring(start, runEnd)}'", position(start), position(runEnd))
        }
        return token(TokenType.NUMBERLITERAL, line.substring(start, end), start, end)
    }

    /** El valor de un string no lleva las comillas; su posición, sí. */
    private fun stringAt(start: Int): Token {
        val closing = line.indexOf(line[start], startIndex = start + 1)
        if (closing < 0) {
            throw LexerException("String sin cerrar: falta la comilla de cierre", position(start), position(line.length))
        }
        return token(TokenType.STRINGLITERAL, line.substring(start + 1, closing), start, closing + 1)
    }

    private fun symbolAt(start: Int): Token {
        val (symbol, type) =
            vocabulary.symbolAt(line, start)
                ?: throw LexerException("Carácter inválido encontrado: '${line[start]}'", position(start), position(start + 1))
        return token(type, symbol, start, start + symbol.length)
    }

    /** Dónde termina la racha de caracteres que cumplen [belongs] a partir de [from]. */
    private fun endOfRun(
        from: Int,
        belongs: (Char) -> Boolean,
    ): Int = (from until line.length).firstOrNull { !belongs(line[it]) } ?: line.length

    private fun token(
        type: TokenType,
        value: String,
        start: Int,
        end: Int,
    ): Token = Token(type, value, position(start), position(end))

    private fun position(column: Int): TokenPosition = TokenPosition(row, column)
}

private fun Char.startsName(): Boolean = this in 'a'..'z' || this in 'A'..'Z' || this == '_'

private fun Char.continuesName(): Boolean = startsName() || isNumeral()

private fun Char.isNumeral(): Boolean = this in '0'..'9'
