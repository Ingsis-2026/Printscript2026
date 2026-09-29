package formatter

/**
 * Arma las líneas del resultado a partir de los huecos y los textos de los tokens.
 *
 * Cada salto de un hueco termina la línea en curso, así que [write] devuelve las líneas que
 * dejó completas y el que las recibe puede emitirlas en el acto. La última la entrega [finish].
 */
internal class OutputLines {
    private val current = StringBuilder()

    fun write(
        gap: Gap,
        text: String,
    ): List<String> {
        val finished = if (gap.breaks == 0) emptyList() else listOf(takeCurrent()) + List(gap.breaks - 1) { "" }
        current.append(" ".repeat(gap.spaces)).append(text)
        return finished
    }

    fun finish(): String = current.toString()

    private fun takeCurrent(): String = current.toString().also { current.clear() }
}
