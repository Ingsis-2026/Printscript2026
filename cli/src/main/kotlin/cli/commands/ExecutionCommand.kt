package cli.commands

import cli.io.Output
import cli.io.Source
import cli.pipeline.ParsingPipeline
import interpreter.Interpreter
import interpreter.Printer
import interpreter.Reader

/**
 * Ejecuta el programa.
 *
 * Cada sentencia se interpreta a medida que el parser la resuelve: el fuente se recorre una
 * sola vez y no se retiene el AST completo.
 */
class ExecutionCommand(
    private val source: Source,
    private val pipeline: ParsingPipeline,
    private val version: String,
    private val printer: Printer,
    private val reader: Reader,
    private val output: Output,
) : Command {
    override fun execute(): CommandStatus {
        val interpreter = Interpreter.forVersion(version, printer, reader)
        pipeline.consume(source) { nodes -> nodes.forEach { interpreter.execute(it) } }
        output.info("Ejecución finalizada.")
        return CommandStatus.SUCCESS
    }
}
