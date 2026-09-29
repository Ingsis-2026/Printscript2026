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
        // El primer salto cierra la línea en curso; cada salto de más deja una línea en blanco.
        val finished = List(gap.breaks) { index -> if (index == 0) takeCurrent() else "" }
        current.append(" ".repeat(gap.spaces)).append(text)
        return finished
    }

    fun finish(): String = current.toString()

    private fun takeCurrent(): String = current.toString().also { current.clear() }
}
