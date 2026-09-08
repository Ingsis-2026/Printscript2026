package cli.commands

import cli.io.FormattedSink
import cli.io.Output
import cli.io.Source
import formatter.Formatter

/**
 * Formatea el fuente según el archivo de reglas y lo entrega al destino indicado.
 *
 * El [Formatter] ya viene armado con sus reglas: acá sólo se conecta el fuente con el destino,
 * y el flujo perezoso de líneas se escribe a medida que se resuelve.
 */
class FormattingCommand(
    private val source: Source,
    private val formatter: Formatter,
    private val sink: FormattedSink,
    private val output: Output,
) : Command {
    override fun execute(): CommandStatus {
        source.useLines { lines -> sink.write(formatter.formatLines(lines)) }
        output.info("Formateo finalizado.")
        return CommandStatus.SUCCESS
    }
}
