package rules

/**
 * Reglas de formateo, con la ausencia de cada una como "no tocar".
 *
 * El formatter conserva el fuente tal como está y sólo reescribe lo que una regla activa
 * gobierna, así que una regla que el archivo no menciona no puede tener un valor por defecto
 * que cambie el resultado: se representa apagada (`false`) o ausente (`null`).
 */
data class FormattingRules(
    val spaceBeforeColon: Boolean = false,
    val spaceAfterColon: Boolean = false,
    val spacesAroundEquals: Int? = null,
    val singleSpaceSeparation: Boolean = false,
    val spaceSurroundingOperations: Boolean = false,
    val lineBreakAfterStatement: Boolean = false,
    val blankLinesAfterPrintln: Int? = null,
    val bracePlacement: BracePlacement? = null,
    val indentInsideIf: Int? = null,
) {
    companion object {
        const val SPACE_BEFORE_COLON = "enforce-spacing-before-colon-in-declaration"
        const val SPACE_AFTER_COLON = "enforce-spacing-after-colon-in-declaration"
        const val SPACE_AROUND_EQUALS = "enforce-spacing-around-equals"
        const val NO_SPACE_AROUND_EQUALS = "enforce-no-spacing-around-equals"
        const val SINGLE_SPACE_SEPARATION = "mandatory-single-space-separation"
        const val SPACE_SURROUNDING_OPERATIONS = "mandatory-space-surrounding-operations"
        const val LINE_BREAK_AFTER_STATEMENT = "mandatory-line-break-after-statement"
        const val LINE_BREAKS_AFTER_PRINTLN = "line-breaks-after-println"
        const val BRACE_SAME_LINE = "if-brace-same-line"
        const val BRACE_BELOW_LINE = "if-brace-below-line"
        const val INDENT_INSIDE_IF = "indent-inside-if"

        fun from(values: Map<String, Any?>): FormattingRules =
            FormattingRules(
                spaceBeforeColon = values.flag(SPACE_BEFORE_COLON),
                spaceAfterColon = values.flag(SPACE_AFTER_COLON),
                spacesAroundEquals = values.equalsSpacing(),
                singleSpaceSeparation = values.flag(SINGLE_SPACE_SEPARATION),
                spaceSurroundingOperations = values.flag(SPACE_SURROUNDING_OPERATIONS),
                lineBreakAfterStatement = values.flag(LINE_BREAK_AFTER_STATEMENT),
                blankLinesAfterPrintln = values.count(LINE_BREAKS_AFTER_PRINTLN),
                bracePlacement = values.bracePlacement(),
                indentInsideIf = values.count(INDENT_INSIDE_IF),
            )

        private fun Map<String, Any?>.flag(key: String): Boolean = this[key] == true

        private fun Map<String, Any?>.count(key: String): Int? = (this[key] as? Number)?.toInt()

        /**
         * Las dos escrituras del espaciado del "=" son reglas distintas en la configuración,
         * pero acá son el mismo ajuste con dos valores.
         */
        private fun Map<String, Any?>.equalsSpacing(): Int? =
            when {
                flag(SPACE_AROUND_EQUALS) -> 1
                flag(NO_SPACE_AROUND_EQUALS) -> 0
                else -> null
            }

        private fun Map<String, Any?>.bracePlacement(): BracePlacement? =
            when {
                flag(BRACE_SAME_LINE) -> BracePlacement.SAME_LINE
                flag(BRACE_BELOW_LINE) -> BracePlacement.NEXT_LINE
                else -> null
            }
    }
}
