package cli.commands

import cli.arguments.CliArguments
import cli.arguments.Operation
import cli.io.ConsoleReader
import cli.io.FileSink
import cli.io.FileSource
import cli.io.FormattedSink
import cli.io.Output
import cli.io.OutputPrinter
import cli.io.OutputSink
import cli.io.Source
import cli.pipeline.ParsingPipeline
import cli.progress.NoOpProgressReporter
import cli.progress.ProgressReporter
import formatter.FormatterBuilder
import formatter.FormatterBuilderPS
import interpreter.Reader
import linter.Linter
import linter.LinterVersion
import java.io.File

/**
 * Arma el comando que corresponde al pedido.
 *
 * Concentra el armado de las dependencias de cada operación —el intérprete, el Formatter con
 * sus reglas, el Linter con las suyas— para que los comandos reciban todo listo y no conozcan
 * ni los argumentos ni la forma de construir sus colaboradores.
 */
class CommandFactory(
    private val output: Output,
    private val progressReporter: ProgressReporter = NoOpProgressReporter,
    private val reader: Reader = ConsoleReader(),
    private val formatterBuilder: FormatterBuilder = FormatterBuilderPS(),
) {
    fun create(arguments: CliArguments): Command {
        val source = FileSource(arguments.sourcePath)
        val pipeline = ParsingPipeline(arguments.version, progressReporter)

        return when (arguments.operation) {
            Operation.VALIDATION -> ValidationCommand(source, pipeline, output)
            Operation.EXECUTION -> executionCommand(source, pipeline, arguments)
            Operation.FORMATTING -> formattingCommand(source, arguments)
            Operation.ANALYZING -> AnalyzingCommand(source, pipeline, buildLinter(arguments), output)
        }
    }

    private fun executionCommand(
        source: Source,
        pipeline: ParsingPipeline,
        arguments: CliArguments,
    ): Command =
        ExecutionCommand(
            source = source,
            pipeline = pipeline,
            version = arguments.version,
            printer = OutputPrinter(output),
            reader = reader,
            output = output,
        )

    private fun formattingCommand(
        source: Source,
        arguments: CliArguments,
    ): Command =
        FormattingCommand(
            source = source,
            formatter = formatterBuilder.build(configPathOf(arguments), arguments.version),
            sink = sinkFor(arguments),
            output = output,
        )

    private fun sinkFor(arguments: CliArguments): FormattedSink {
        val outputPath = arguments.outputPath ?: return OutputSink(output)
        return FileSink(outputPath)
    }

    private fun buildLinter(arguments: CliArguments): Linter {
        val linterVersion =
            requireNotNull(LinterVersion.fromString(arguments.version)) {
                "El linter no soporta la versión ${arguments.version}."
            }
        val linter = Linter(linterVersion)
        linter.readJson(File(configPathOf(arguments)).readText())
        return linter
    }

    /** El parseo de argumentos ya exigió la configuración para las operaciones que la piden. */
    private fun configPathOf(arguments: CliArguments): String =
        requireNotNull(arguments.configPath) {
            "La operación ${arguments.operation.argument} necesita un archivo de configuración."
        }
}
