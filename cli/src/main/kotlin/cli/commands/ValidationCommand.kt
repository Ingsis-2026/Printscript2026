package cli.commands

import cli.io.Output
import cli.io.Source
import cli.pipeline.ParsingPipeline

/**
 * Modo que sólo valida el fuente: lo recorre completo sin ejecutar ni escribir nada.
 *
 * Consumir el flujo de nodos alcanza para validar, porque el Lexer y el Parser fallan sobre la
 * sentencia que no puedan resolver.
 */
class ValidationCommand(
    private val source: Source,
    private val pipeline: ParsingPipeline,
    private val output: Output,
) : Command {
    override fun execute(): CommandStatus {
        val result = pipeline.consume(source) { nodes -> nodes.count() }
        val statements = pluralize(result.statements, "sentencia", "sentencias")
        output.info("Validación exitosa: ${result.statements} $statements en ${source.name}.")
        return CommandStatus.SUCCESS
    }
}
