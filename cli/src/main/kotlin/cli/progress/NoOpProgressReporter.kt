package cli.progress

/** No informa nada. Sirve para los tests y para cualquier uso no interactivo. */
object NoOpProgressReporter : ProgressReporter {
    override fun onStatementParsed(count: Int) = Unit

    override fun onFinished(total: Int) = Unit
}
