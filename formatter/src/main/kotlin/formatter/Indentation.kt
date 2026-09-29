package formatter

/**
 * Sangría de la línea que empieza en un token.
 *
 * Con la regla de sangría activa se calcula por profundidad. Sin la regla depende de quién puso
 * el salto.
 */
internal class Indentation(
    private val size: Int?,
) {
    /** El salto ya estaba en el fuente: sin regla, se conserva la columna original. */
    fun forSourceBreak(
        next: SourceToken,
        state: FormattingState,
    ): Int = byDepth(next, state) ?: next.startColumn

    /**
     * El salto lo agrega el formatter: la columna original es la del medio de otra línea y no
     * sirve, así que sin regla se repite la sangría de la línea que se viene cerrando.
     */
    fun forAddedBreak(
        next: SourceToken,
        state: FormattingState,
    ): Int = byDepth(next, state) ?: state.currentLineIndent

    private fun byDepth(
        next: SourceToken,
        state: FormattingState,
    ): Int? {
        val size = size ?: return null
        return size * state.depthOf(next)
    }
}
