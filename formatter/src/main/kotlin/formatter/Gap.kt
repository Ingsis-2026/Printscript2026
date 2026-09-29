package formatter

/**
 * Lo que va entre dos tokens: primero los saltos de línea y después los espacios. Si hay saltos,
 * los espacios son la sangría de la línea nueva.
 */
internal data class Gap(
    val breaks: Int,
    val spaces: Int,
) {
    companion object {
        fun sameLine(spaces: Int): Gap = Gap(breaks = 0, spaces = spaces)

        fun lineBreaks(
            breaks: Int,
            indent: Int,
        ): Gap = Gap(breaks = breaks, spaces = indent)
    }
}

/** Una de las decisiones sobre el hueco entre dos tokens vecinos. */
internal fun interface GapRule {
    /** El hueco entre [previous] y [next], o `null` si esta regla no lo gobierna. */
    fun decide(
        previous: SourceToken,
        next: SourceToken,
        state: FormattingState,
    ): Gap?
}
