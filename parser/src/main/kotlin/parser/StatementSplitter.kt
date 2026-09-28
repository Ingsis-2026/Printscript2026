package parser

import token.Token

/**
 * Agrupa un flujo de tokens en sentencias (una lista de tokens por sentencia).
 *
 * Trabaja de forma perezosa: consume el [Sequence] de entrada a medida que el consumidor
 * pide sentencias y emite cada una en cuanto está completa, sin retener las anteriores.
 * Sólo se mantiene en memoria la sentencia en curso, que es la unidad mínima que el parser
 * necesita ver completa.
 */
internal class StatementSplitter(
    private val insideBlock: Boolean = false,
) {
    fun split(tokens: Sequence<Token>): Sequence<List<Token>> =
        sequence {
            val statement = PendingStatement(insideBlock)
            for (token in tokens) {
                statement.completedBefore(token)?.let { yield(it) }
                statement.add(token)?.let { yield(it) }
            }
            statement.completedAtEnd()?.let { yield(it) }
        }.ifEmpty { throw ParserException("Error: No valid code.") }
}

/**
 * La sentencia que se está armando.
 *
 * Termina en su `;`, o cuando se cierra el bloque que abrió, salvo que siga un `else`. Mientras
 * tiene un bloque abierto, todo lo que llega es parte de ese bloque: el `;` de sus sentencias lo
 * necesita quien parsee el cuerpo, y un `if` anidado no empieza una sentencia en este nivel.
 *
 * Con [insideBlock], los tokens son el cuerpo de un bloque y llegan con el `}` que lo cierra: ese
 * `}` termina la última sentencia, que por eso puede omitir el `;`.
 */
private class PendingStatement(
    private val insideBlock: Boolean,
) {
    private val tokens = mutableListOf<Token>()
    private var openBlocks = 0
    private var closedItsBlock = false

    /** Una sentencia cuyo bloque se cerró termina ahí, salvo que [next] sea el `else` que la continúa. */
    fun completedBefore(next: Token): List<Token>? {
        if (!closedItsBlock || next.continuesConditional) return null
        return take()
    }

    /** Suma [token] a la sentencia y la devuelve si ese token la terminó. */
    fun add(token: Token): List<Token>? {
        closedItsBlock = false
        if (openBlocks > 0) return addToOpenBlock(token)
        return when {
            token.opensBlock -> openBlock(token)
            token.closesBlock -> closeEnclosingBlock(token)
            token.startsConditional -> startConditional(token)
            token.endsStatement -> take()
            else -> keep(token)
        }
    }

    /** Lo que queda al acabarse los tokens tiene que terminar en una llave; si no, le falta el `;`. */
    fun completedAtEnd(): List<Token>? {
        val last = tokens.lastOrNull() ?: return null
        if (!last.closesBlock && !last.opensBlock) throw unterminatedStatement()
        return take()
    }

    private fun addToOpenBlock(token: Token): List<Token>? {
        if (token.opensBlock) openBlocks++
        if (token.closesBlock) openBlocks--
        if (openBlocks == 0) closedItsBlock = true
        return keep(token)
    }

    private fun openBlock(token: Token): List<Token>? {
        openBlocks++
        return keep(token)
    }

    /** Sin un bloque propio abierto, un `}` sólo puede ser el del bloque que envuelve a este cuerpo. */
    private fun closeEnclosingBlock(token: Token): List<Token>? {
        if (!insideBlock) throw unmatchedBrace(token)
        return take()
    }

    /** Un `if` empieza una sentencia: si había otra en curso, le faltó el terminador. */
    private fun startConditional(token: Token): List<Token>? {
        if (tokens.isNotEmpty()) throw unterminatedStatement()
        return keep(token)
    }

    private fun keep(token: Token): List<Token>? {
        tokens.add(token)
        return null
    }

    private fun take(): List<Token>? {
        if (tokens.isEmpty()) return null
        return tokens.toList().also { tokens.clear() }
    }

    private fun unterminatedStatement(): ParserException =
        ParserException("las sentencias deben finalizar con \";\", \"}\" o \"{\"", tokens)

    private fun unmatchedBrace(token: Token): ParserException =
        ParserException("se encontró un \"}\" que no cierra ningún bloque", listOf(token))
}
