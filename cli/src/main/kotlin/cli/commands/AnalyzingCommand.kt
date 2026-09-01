package cli.commands

import cli.io.Output
import cli.io.Source
import cli.pipeline.ParsingPipeline
import linter.BrokenRule
import linter.Linter

/**
 * Analiza el fuente e informa cada regla incumplida con su ubicación exacta.
 *
 * Encontrar violaciones no es un error de la herramienta: el análisis termina bien y el
 * desenlace se comunica con [CommandStatus.FAILURE], que la CLI traduce al código de salida.
 */
class AnalyzingCommand(
    private val source: Source,
    private val pipeline: ParsingPipeline,
    private val linter: Linter,
    private val output: Output,
) : Command {
    override fun execute(): CommandStatus {
        val result = pipeline.consume(source) { nodes -> linter.check(nodes) }
        val brokenRules = result.value.getBrokenRules()

        if (brokenRules.isEmpty()) {
            output.info("No se encontraron problemas en ${source.name}.")
            return CommandStatus.SUCCESS
        }

        output.error("Se encontraron ${brokenRules.size} ${pluralize(brokenRules.size, "problema", "problemas")} en ${source.name}:")
        brokenRules.forEach { output.error(describe(it)) }
        return CommandStatus.FAILURE
    }

    /** Ubicación en coordenadas 1-based, igual que la de los errores de PrintScript. */
    private fun describe(brokenRule: BrokenRule): String {
        val position = brokenRule.errorPosition
        return "${brokenRule.ruleDescription} (línea ${position.row + 1}, columna ${position.column + 1})"
    }
}
