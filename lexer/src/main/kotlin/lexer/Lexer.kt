package lexer

import token.Token
import token.TokenPosition
import token.TokenType
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.regex.Pattern

/**
 * Convierte código fuente en [Token]s.
 *
 * Cómo funciona: al construirse le pide al [TokenMapper] un único regex que es la unión de los
 * regex de todos los tipos de token ([anyTokenPattern]). Con ese regex recorre cada línea de
 * izquierda a derecha; cada match es un lexema, y dónde empieza y termina ese match es la
 * posición del token en el fuente. Con el texto del lexema ya aislado, le vuelve a preguntar al
 * [TokenMapper] de qué tipo es.
 *
 * O sea, dos etapas: el regex combinado resuelve *dónde* está cada lexema ([findLexemes]) y el
 * [TokenMapper] resuelve *qué* es ([toToken]). Hacen falta las dos porque el regex no alcanza
 * para decidir el tipo: a una palabra reservada como "true" la encuentra el regex de
 * identificadores y sin embargo no es un identificador.
 *
 * Lo que el regex no reconoce no hace fallar la búsqueda: queda como un hueco entre dos matches,
 * y por eso los huecos se revisan aparte (ver [rejectSkippedText]).
 */
class Lexer(
    private val tokenMapper: TokenMapper,
) {
    private val anyTokenPattern: Pattern = tokenMapper.combinedPattern()

    fun execute(input: String): List<Token> = convertToTokens(input.lineSequence()).toList()

    fun convertToTokens(lines: Sequence<String>): Sequence<Token> = lines.mapIndexed(::tokenizeLine).flatten()

    fun convertToTokens(inputStream: InputStream): Sequence<Token> =
        convertToTokens(BufferedReader(InputStreamReader(inputStream)).lineSequence())

    /** Una línea se convierte en sus tokens en dos pasos: encontrar sus lexemas y resolver qué es cada uno. */
    private fun tokenizeLine(
        row: Int,
        lineContent: String,
    ): List<Token> = findLexemes(lineContent, row).map(::toToken).toList()

    /**
     * Paso 1: los lexemas de la línea, en orden, verificando que entre uno y otro no haya quedado
     * texto sin reconocer.
     *
     * Se entregan de a uno y no todos juntos para que cada lexema se convierta en token apenas se
     * lo encuentra: así, si una línea tiene más de un problema, se reporta el que aparece primero.
     */
    private fun findLexemes(
        lineContent: String,
        row: Int,
    ): Sequence<Lexeme> =
        sequence {
            val matcher = anyTokenPattern.matcher(lineContent)
            var scannedUpTo = 0

            while (matcher.find()) {
                rejectSkippedText(lineContent, row, from = scannedUpTo, to = matcher.start())
                yield(
                    Lexeme(
                        text = matcher.group(),
                        start = TokenPosition(row, matcher.start()),
                        end = TokenPosition(row, matcher.end()),
                    ),
                )
                scannedUpTo = matcher.end()
            }
            rejectSkippedText(lineContent, row, from = scannedUpTo, to = lineContent.length)
        }

    /**
     * Rechaza el texto que el patrón no reconoció.
     *
     * `Matcher.find` no falla ante un lexema inválido: simplemente lo saltea y sigue con el
     * match siguiente. Para detectarlo hay que mirar los huecos que deja entre matches —y la
     * cola de la línea—, donde cualquier cosa que no sea espacio en blanco es un error.
     */
    private fun rejectSkippedText(
        lineContent: String,
        row: Int,
        from: Int,
        to: Int,
    ) {
        if (from >= to) return
        val skipped = lineContent.substring(from, to)
        if (skipped.isBlank()) return

        val leadingBlanks = skipped.indexOfFirst { !it.isWhitespace() }
        val invalid = skipped.trim()
        val startColumn = from + leadingBlanks
        throw LexerException(
            "Carácter inválido encontrado: '$invalid'",
            TokenPosition(row, startColumn),
            TokenPosition(row, startColumn + invalid.length),
        )
    }

    /** Paso 2: un lexema ya ubicado se convierte en el token que le corresponde. */
    private fun toToken(lexeme: Lexeme): Token {
        val tokenType = resolveType(lexeme)
        return Token(tokenType, extractTokenValue(tokenType, lexeme.text), lexeme.start, lexeme.end)
    }

    private fun resolveType(lexeme: Lexeme): TokenType =
        when (val resolution = tokenMapper.resolve(lexeme.text)) {
            is TokenResolution.Recognized -> resolution.type
            is TokenResolution.Rejected -> throw LexerException(resolution.reason, lexeme.start, lexeme.end)
        }

    /** El valor que el token lleva de verdad: un string literal pierde las comillas que lo delimitan. */
    private fun extractTokenValue(
        tokenType: TokenType,
        rawValue: String,
    ): String =
        when (tokenType) {
            TokenType.STRINGLITERAL -> rawValue.removeSurrounding("\"", "\"").removeSurrounding("'", "'")
            else -> rawValue
        }

    /** Un texto que el patrón reconoció, ya ubicado en el fuente pero todavía sin clasificar. */
    private data class Lexeme(
        val text: String,
        val start: TokenPosition,
        val end: TokenPosition,
    )
}
