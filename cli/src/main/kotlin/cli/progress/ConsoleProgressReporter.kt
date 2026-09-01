package cli.progress

import java.io.PrintStream

/**
 * Muestra el avance del parseo sobrescribiendo siempre la misma línea de `stderr`.
 *
 * Va a `stderr` para no contaminar el resultado en `stdout`, y usa "\r" en lugar de un salto
 * de línea para no llenar la pantalla en un fuente de miles de sentencias.
 */
class ConsoleProgressReporter(
    private val err: PrintStream = System.err,
) : ProgressReporter {
    override fun onStatementParsed(count: Int) {
        err.print("\rAnalizando... $count sentencias")
        err.flush()
    }

    override fun onFinished(total: Int) {
        err.print("\rAnalizando... $total sentencias. Listo.\n")
        err.flush()
    }
}
