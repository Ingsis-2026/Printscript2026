package formatter

import rules.FormattingRules

interface Formatter {
    fun format(input: String): String

    /**
     * Formatea un fuente emitiendo cada línea del resultado a medida que la resuelve.
     *
     * Está en la interfaz —y no sólo en la implementación— porque es el punto de entrada para
     * un fuente que no cabe en memoria: quien recibe un [Formatter] tiene que poder consumirlo
     * de a una línea sin conocer la clase concreta.
     */
    fun formatLines(lines: Sequence<String>): Sequence<String>

    fun getRules(): FormattingRules
}
