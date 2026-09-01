package cli.io

import java.io.File

/**
 * Escribe el código formateado en un archivo, sentencia por sentencia.
 *
 * Nunca arma el resultado completo en memoria: es lo que permite formatear un fuente grande
 * hacia un archivo de salida.
 */
class FileSink(
    private val file: File,
) : FormattedSink {
    constructor(path: String) : this(File(path))

    override fun write(statements: Sequence<String>) {
        file.bufferedWriter().use { writer ->
            statements.forEach { statement ->
                writer.write(statement)
                writer.newLine()
            }
        }
    }
}
