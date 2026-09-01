package cli.io

/**
 * Destino del código formateado.
 *
 * Recibe el flujo de sentencias sin materializarlo, así que el resultado se puede escribir a
 * medida que el Formatter lo produce.
 */
interface FormattedSink {
    fun write(statements: Sequence<String>)
}
