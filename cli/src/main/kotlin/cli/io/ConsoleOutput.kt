package cli.io

import java.io.PrintStream

/**
 * Escribe el resultado en `stdout` y todo lo demás en `stderr`.
 *
 * La separación es la que permite canalizar la salida del Formatter a un archivo sin que se
 * mezclen los mensajes de avance ni el progreso del parseo.
 */
class ConsoleOutput(
    private val out: PrintStream = System.out,
    private val err: PrintStream = System.err,
) : Output {
    override fun info(message: String) = err.println(message)

    override fun result(message: String) = out.println(message)

    override fun error(message: String) = err.println(message)
}
