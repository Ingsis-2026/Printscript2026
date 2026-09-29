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

    /** Quién decide cada hueco, de lo más puntual a lo más amplio: el primero que responde gana. */
    private val gapRules =
        listOf(
            BracePlacement(rules.braceOnSameLine, indentation),
            BreaksAfterStatement(rules, indentation),
            SourceLineBreaks(indentation),
            SameLineSpacing(rules),
        )

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

    private fun gapBetween(
        previous: SourceToken,
        next: SourceToken,
        state: FormattingState,
    ): Gap = gapRules.firstNotNullOf { it.decide(previous, next, state) }
}
