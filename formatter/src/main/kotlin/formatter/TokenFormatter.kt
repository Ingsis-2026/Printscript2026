package formatter

import lexer.Lexer
import rules.FormattingRules

/**
 * Formatea reescribiendo la separación entre tokens, no reimprimiendo el árbol.
 *
 * La consigna pide aplicar las reglas configuradas y dejar el resto del fuente como estaba:
 * dos declaraciones pueden quedar con distinto espaciado antes de los ":" si la regla que se
 * activó es la del espacio *después*. Reimprimir desde el AST no puede hacer eso, porque el
 * AST ya perdió cómo estaba escrito el original; en cambio la posición de cada token alcanza
 * para reconstruirlo, así que el formateo se decide sobre el hueco entre dos tokens vecinos.
 *
 * Todo el recorrido es perezoso: se emite cada línea del resultado a medida que se resuelve.
 */
class TokenFormatter(
    private val rules: FormattingRules,
    lexer: Lexer,
) : Formatter {
    private val scanner = SourceScanner(lexer)

    override fun getRules(): FormattingRules = rules

    override fun format(input: String): String {
        if (input.isBlank()) return ""
        return formatLines(input.lineSequence()).joinToString("\n")
    }

    override fun formatLines(lines: Sequence<String>): Sequence<String> =
        sequence {
            val state = FormattingState()
            val line = StringBuilder()
            var previous: SourceToken? = null

            for (current in scanner.scan(lines)) {
                val separator = previous?.let { separatorBetween(it, current, state) } ?: leadingGap(current)

                // Un separador con saltos cierra la línea en curso tantas veces como saltos tenga.
                separator.split("\n").forEachIndexed { index, piece ->
                    if (index > 0) {
                        yield(line.toString())
                        line.clear()
                    }
                    line.append(piece)
                }

                line.append(current.text)
                state.advance(current)
                previous = current
            }

            if (previous != null) yield(line.toString())
        }

    /** Sangría y saltos que preceden al primer token del fuente. */
    private fun leadingGap(first: SourceToken): String = "\n".repeat(first.startRow) + " ".repeat(first.startColumn)

    private fun separatorBetween(
        previous: SourceToken,
        next: SourceToken,
        state: FormattingState,
    ): String {
        val brokeLine = next.startRow > previous.endRow

        bracePlacement(next, state)?.let { return it }
        if (previous.isSemicolon) {
            breaksAfterStatement(state, brokeLine)?.let { breaks ->
                return newline(breaks, indentFor(next, state, useOriginal = brokeLine))
            }
        }
        if (brokeLine) {
            return newline(next.startRow - previous.endRow, indentFor(next, state, useOriginal = true))
        }
        return spacingBetween(previous, next, state)
    }

    /** La llave que abre un bloque queda donde diga la regla, o donde estaba si no hay regla. */
    private fun bracePlacement(
        next: SourceToken,
        state: FormattingState,
    ): String? {
        if (!next.isOpeningBrace) return null
        return when (rules.braceOnSameLine) {
            true -> " "
            false -> newline(1, indentFor(next, state, useOriginal = false))
            null -> null
        }
    }

    /**
     * Saltos que deben quedar después de un ";", o `null` para conservar los del fuente.
     *
     * La regla de println pide *n* líneas en blanco, que son *n + 1* saltos.
     */
    private fun breaksAfterStatement(
        state: FormattingState,
        brokeLine: Boolean,
    ): Int? {
        rules.lineBreaksAfterPrintln?.let { blankLines ->
            if (state.statementWasPrintln) return blankLines + 1
        }
        if (rules.lineBreakAfterStatement) return if (brokeLine) null else 1
        return null
    }

    /**
     * Sangría de la línea que empieza en [next].
     *
     * Con la regla de sangría activa se calcula por profundidad —la llave que cierra pertenece
     * al bloque de afuera, así que va un nivel menos—. Sin la regla se conserva la del fuente,
     * salvo que el salto lo esté agregando el formatter: en ese caso la columna original es la
     * del medio de otra línea y no sirve, y se usa la sangría de la línea que se viene cerrando.
     */
    private fun indentFor(
        next: SourceToken,
        state: FormattingState,
        useOriginal: Boolean,
    ): Int {
        rules.indentInsideIf?.let { size ->
            val depth = if (next.isClosingBrace) state.depth - 1 else state.depth
            return size * depth.coerceAtLeast(0)
        }
        return if (useOriginal) next.startColumn else state.currentLineIndent
    }

    private fun newline(
        breaks: Int,
        indent: Int,
    ): String = "\n".repeat(breaks) + " ".repeat(indent)

    /** Separación entre dos tokens de la misma línea. */
    private fun spacingBetween(
        previous: SourceToken,
        next: SourceToken,
        state: FormattingState,
    ): String {
        if (rules.spaceBeforeColon && state.inDeclaration && next.isColon) return " "
        if (rules.spaceAfterColon && state.inDeclaration && previous.isColon) return " "

        rules.spaceAroundEquals?.let { spaced ->
            if (previous.isAssignation || next.isAssignation) return if (spaced) " " else ""
        }
        if (rules.spaceSurroundingOperations && (previous.isOperator || next.isOperator)) return " "

        // La separación uniforme es la regla más amplia, así que va última: cualquier regla
        // puntual sobre este hueco ya decidió antes.
        if (rules.singleSpaceSeparation) return if (next.isSemicolon) "" else " "

        return " ".repeat(next.startColumn - previous.endColumn)
    }
}
