package cli.io

import interpreter.Reader
import java.io.BufferedReader

/**
 * Entrada estándar para `readInput`.
 *
 * Mantiene un único lector sobre `stdin`: crear un `Scanner` nuevo por llamada —como hacía la
 * CLI anterior— descarta lo que quedó en el buffer y pierde líneas entre lecturas sucesivas.
 */
class ConsoleReader(
    private val reader: BufferedReader = System.`in`.bufferedReader(),
) : Reader {
    override fun input(message: String): String = reader.readLine() ?: ""
}
