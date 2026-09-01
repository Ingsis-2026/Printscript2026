package cli

import cli.arguments.ArgumentParser
import cli.arguments.CliUsageException
import cli.commands.CommandFactory
import cli.commands.CommandStatus
import cli.io.Output
import cli.progress.ConsoleProgressReporter
import diagnostics.PrintScriptException
import java.io.IOException

/**
 * Punto de entrada de la CLI: interpreta los argumentos, corre la operación y traduce el
 * desenlace a un código de salida.
 *
 * Es también el único lugar donde se atrapan errores. Los comandos no manejan excepciones: así
 * el reporte —mensaje más ubicación exacta en el archivo— se arma una sola vez y queda igual
 * para todas las operaciones.
 */
class PrintScriptCli(
    private val output: Output,
    private val argumentParser: ArgumentParser = ArgumentParser(),
    private val invoker: CliInvoker = CliInvoker(),
    private val commandFactory: CommandFactory = CommandFactory(output, ConsoleProgressReporter()),
) {
    fun run(arguments: List<String>): Int =
        try {
            execute(arguments)
        } catch (exception: PrintScriptException) {
            // describe() incluye fila y columna de inicio y de fin del problema.
            output.error("Error: ${exception.describe()}")
            EXIT_ERROR
        } catch (exception: CliUsageException) {
            reportUsage(exception.message)
            EXIT_USAGE
        } catch (exception: IllegalArgumentException) {
            // Configuración inválida para la versión elegida: el problema está en el archivo
            // de reglas, no en cómo se invocó la CLI, así que no se muestra el uso.
            output.error("Error de configuración: ${exception.message}")
            EXIT_USAGE
        } catch (exception: IllegalStateException) {
            // Reglas mal formadas: RulesReader informa el problema con error().
            output.error("Error de configuración: ${exception.message}")
            EXIT_USAGE
        } catch (exception: IOException) {
            output.error("Error de entrada/salida: ${exception.message}")
            EXIT_ERROR
        }

    private fun execute(arguments: List<String>): Int {
        val command = commandFactory.create(argumentParser.parse(arguments))
        return when (invoker.runCommand(command)) {
            CommandStatus.SUCCESS -> EXIT_SUCCESS
            CommandStatus.FAILURE -> EXIT_ERROR
        }
    }

    private fun reportUsage(message: String?) {
        output.error("Error: ${message ?: "argumentos inválidos"}")
        output.error(ArgumentParser.usage())
    }

    companion object {
        const val EXIT_SUCCESS = 0
        const val EXIT_ERROR = 1
        const val EXIT_USAGE = 2
    }
}
