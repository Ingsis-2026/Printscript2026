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
            val current = mutableListOf<Token>()
            var openBraces = 0
            var blockJustClosed = false
            var emittedAny = false

            for (token in tokens) {
                // Un "}" que cerró el bloque puede continuar con "else": la sentencia quedó
                // retenida hasta poder mirar este token.
                if (blockJustClosed) {
                    blockJustClosed = false
                    if (token.value == "else") {
                        current.add(token)
                        continue
                    }
                    yield(current.toList())
                    emittedAny = true
                    current.clear()
                }

                when {
                    token.value == "{" -> {
                        openBraces++
                        current.add(token)
                    }

                    token.value == "}" && openBraces > 0 -> {
                        openBraces--
                        current.add(token)
                        if (openBraces == 0) blockJustClosed = true
                    }

                    // El "}" que cierra el bloque que envuelve a este cuerpo no forma parte de
                    // la última sentencia: la termina. Por eso esa sentencia puede omitir el ";".
                    token.value == "}" && insideBlock -> {
                        if (current.isNotEmpty()) {
                            yield(current.toList())
                            emittedAny = true
                            current.clear()
                        }
                    }

                    token.value == "}" -> throw unmatchedBrace(token)

                    // Dentro de un bloque los tokens se acumulan tal cual: el ";" que separa
                    // sus sentencias lo necesita quien parsee el cuerpo, y un "if" anidado no
                    // abre una sentencia nueva en este nivel.
                    openBraces > 0 -> current.add(token)

                    token.value == "if" -> {
                        // Una sentencia pendiente antes de un "if" quedó sin terminador.
                        if (current.isNotEmpty()) throw unterminatedStatement(current)
                        current.add(token)
                    }

                    token.value == ";" ->
                        if (current.isNotEmpty()) {
                            yield(current.toList())
                            emittedAny = true
                            current.clear()
                        }

                    else -> current.add(token)
                }
            }

            if (current.isNotEmpty()) {
                val last = current.last()
                if (last.value != "}" && last.value != "{") throw unterminatedStatement(current)
                yield(current.toList())
                emittedAny = true
            }

            if (!emittedAny) throw ParserException("Error: No valid code.")
        }

    private fun unterminatedStatement(statement: List<Token>): ParserException =
        ParserException(
            "las sentencias deben finalizar con \";\", \"}\" o \"{\"",
            statement.first().getPosition(),
            statement.last().getFinalPosition(),
        )

    private fun unmatchedBrace(token: Token): ParserException =
        ParserException(
            "se encontró un \"}\" que no cierra ningún bloque",
            token.getPosition(),
            token.getFinalPosition(),
        )
}
