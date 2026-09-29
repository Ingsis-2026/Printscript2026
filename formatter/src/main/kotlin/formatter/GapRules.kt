package formatter

import rules.FormattingRules

// Las cuatro decisiones sobre un hueco, en el orden en que TokenFormatter las consulta: de la
// más puntual a la más amplia. La primera que responde gana.

/** La llave que abre un bloque queda donde diga la regla; sin regla, decide la que sigue. */
internal class BracePlacement(
    private val onSameLine: Boolean?,
    private val indentation: Indentation,
) : GapRule {
    override fun decide(
        previous: SourceToken,
        next: SourceToken,
        state: FormattingState,
    ): Gap? {
        if (!next.isOpeningBrace) return null
        return when (onSameLine) {
            true -> Gap.sameLine(1)
            false -> Gap.lineBreaks(1, indentation.forAddedBreak(next, state))
            null -> null
        }
    }
}

/**
 * Los saltos después de un ";", si una regla los fija.
 *
 * La regla de println pide *n* líneas en blanco, que son *n + 1* saltos. La del salto después de
 * cada sentencia sólo agrega uno donde el fuente no tenía ninguno: si ya lo había, el hueco queda
 * para la decisión que sigue, que conserva los del fuente.
 */
internal class BreaksAfterStatement(
    private val rules: FormattingRules,
    private val indentation: Indentation,
) : GapRule {
    override fun decide(
        previous: SourceToken,
        next: SourceToken,
        state: FormattingState,
    ): Gap? {
        if (!previous.isSemicolon) return null
        val brokeLine = brokeLineBetween(previous, next)
        val breaks = breaksAfter(state, brokeLine) ?: return null
        val indent = if (brokeLine) indentation.forSourceBreak(next, state) else indentation.forAddedBreak(next, state)
        return Gap.lineBreaks(breaks, indent)
    }

    private fun breaksAfter(
        state: FormattingState,
        brokeLine: Boolean,
    ): Int? {
        val blankLinesAfterPrintln = rules.lineBreaksAfterPrintln
        return when {
            blankLinesAfterPrintln != null && state.closedStatement == StatementKind.PRINTLN -> blankLinesAfterPrintln + 1
            rules.lineBreakAfterStatement && !brokeLine -> 1
            else -> null
        }
    }
}

/** Si el fuente cortó la línea acá, se conservan sus saltos y su sangría. */
internal class SourceLineBreaks(
    private val indentation: Indentation,
) : GapRule {
    override fun decide(
        previous: SourceToken,
        next: SourceToken,
        state: FormattingState,
    ): Gap? {
        if (!brokeLineBetween(previous, next)) return null
        return Gap.lineBreaks(next.startRow - previous.endRow, indentation.forSourceBreak(next, state))
    }
}

/**
 * Un hueco dentro de una misma línea. Siempre responde: si ninguna regla lo gobierna, copia la
 * separación del fuente.
 */
internal class SameLineSpacing(
    private val rules: FormattingRules,
) : GapRule {
    override fun decide(
        previous: SourceToken,
        next: SourceToken,
        state: FormattingState,
    ): Gap = Gap.sameLine(spaces(previous, next, state))

    private fun spaces(
        previous: SourceToken,
        next: SourceToken,
        state: FormattingState,
    ): Int {
        val inDeclaration = state.currentStatement == StatementKind.DECLARATION
        val spaceAroundEquals = rules.spaceAroundEquals
        return when {
            rules.spaceBeforeColon && inDeclaration && next.isColon -> 1
            rules.spaceAfterColon && inDeclaration && previous.isColon -> 1
            spaceAroundEquals != null && (previous.isAssignation || next.isAssignation) -> if (spaceAroundEquals) 1 else 0
            rules.spaceSurroundingOperations && (previous.isOperator || next.isOperator) -> 1
            // La separación uniforme es la regla más amplia, así que va última: cualquier regla
            // puntual sobre este hueco ya decidió antes.
            rules.singleSpaceSeparation -> if (next.isSemicolon) 0 else 1
            else -> next.startColumn - previous.endColumn
        }
    }
}

private fun brokeLineBetween(
    previous: SourceToken,
    next: SourceToken,
): Boolean = next.startRow > previous.endRow
