package cli.io

import interpreter.Printer

/**
 * Conecta la salida del programa interpretado con el canal de resultado de la CLI.
 *
 * Lo que un `println` de PrintScript imprime *es* el resultado de la ejecución, así que va a
 * `stdout` y no se mezcla con los mensajes de la herramienta.
 */
class OutputPrinter(
    private val output: Output,
) : Printer {
    override fun print(message: String) = output.result(message)
}
