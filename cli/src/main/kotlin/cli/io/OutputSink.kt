package cli.io

/** Escribe el código formateado en el canal de resultado (por defecto, `stdout`). */
class OutputSink(
    private val output: Output,
) : FormattedSink {
    override fun write(statements: Sequence<String>) = statements.forEach { output.result(it) }
}
