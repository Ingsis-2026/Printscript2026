package cli.progress

/**
 * Informa el avance del parseo, que la consigna pide mostrar mientras ocurre.
 *
 * El parseo es perezoso, así que el avance se cuenta por sentencias resueltas: es lo que se
 * puede saber sin haber leído el fuente completo.
 */
interface ProgressReporter {
    fun onStatementParsed(count: Int)

    fun onFinished(total: Int)
}
