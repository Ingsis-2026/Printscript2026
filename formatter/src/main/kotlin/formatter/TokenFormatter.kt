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
    private val indentation = Indentation(rules.indentInsideIf)

    override fun getRules(): FormattingRules = rules

    override fun format(input: String): String {
        if (input.isBlank()) return ""
        return formatLines(input.lineSequence()).joinToString("\n")
    }

    override fun formatLines(lines: Sequence<String>): Sequence<String> =
        sequence {
            val state = FormattingState()
            val output = OutputLines()
            var previous: SourceToken? = null

            for (current in scanner.scan(lines)) {
                val gap = previous?.let { gapBetween(it, current, state) } ?: leadingGap(current)
                yieldAll(output.write(gap, current.text))
                state.advance(current)
                previous = current
            }

            if (previous != null) yield(output.finish())
        }

    /** Los saltos y la sangría que preceden al primer token del fuente. */
    private fun leadingGap(first: SourceToken): Gap = Gap.lineBreaks(first.startRow, first.startColumn)

    /** Las reglas van de la más puntual a la más amplia, así que la primera que aplica decide. */
    private fun gapBetween(
        previous: SourceToken,
        next: SourceToken,
        state: FormattingState,
    ): Gap {
        val sourceBreaks = next.startRow - previous.endRow
        val indent = if (sourceBreaks > 0) indentation.forSourceBreak(next, state) else indentation.forAddedBreak(next, state)
        val blankLinesAfterPrintln = rules.lineBreaksAfterPrintln
        val endsPrintln = previous.isSemicolon && state.statement == StatementKind.PRINTLN
        return when {
            next.isOpeningBrace && rules.braceOnSameLine == true -> Gap.sameLine(1)
            next.isOpeningBrace && rules.braceOnSameLine == false -> Gap.lineBreaks(1, indentation.forAddedBreak(next, state))
            blankLinesAfterPrintln != null && endsPrintln -> Gap.lineBreaks(blankLinesAfterPrintln + 1, indent)
            rules.lineBreakAfterStatement && previous.isSemicolon && sourceBreaks == 0 -> Gap.lineBreaks(1, indent)
            sourceBreaks > 0 -> Gap.lineBreaks(sourceBreaks, indent)
            else -> Gap.sameLine(spacesBetween(previous, next, state))
        }
    }

    private fun spacesBetween(
        previous: SourceToken,
        next: SourceToken,
        state: FormattingState,
    ): Int {
        val inDeclaration = state.statement == StatementKind.DECLARATION
        val spaceAroundEquals = rules.spaceAroundEquals
        return when {
            rules.spaceBeforeColon && inDeclaration && next.isColon -> 1
            rules.spaceAfterColon && inDeclaration && previous.isColon -> 1
            spaceAroundEquals != null && (previous.isAssignation || next.isAssignation) -> if (spaceAroundEquals) 1 else 0
            rules.spaceSurroundingOperations && (previous.isOperator || next.isOperator) -> 1
            rules.singleSpaceSeparation -> if (next.isSemicolon) 0 else 1
            else -> next.startColumn - previous.endColumn
        }
    }
}
