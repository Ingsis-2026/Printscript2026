package lexer

import token.Token
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

/**
 * Convierte código fuente en [Token]s, una línea por vez: cada línea la recorre un [LineScanner]
 * con el vocabulario de la versión.
 */
class Lexer(
    private val tokenMapper: TokenMapper,
) {
    fun execute(input: String): List<Token> = convertToTokens(input.lineSequence()).toList()

    fun convertToTokens(lines: Sequence<String>): Sequence<Token> =
        lines.mapIndexed { row, line -> LineScanner(tokenMapper, row, line).tokens() }.flatten()

    fun convertToTokens(inputStream: InputStream): Sequence<Token> =
        convertToTokens(BufferedReader(InputStreamReader(inputStream)).lineSequence())
}
