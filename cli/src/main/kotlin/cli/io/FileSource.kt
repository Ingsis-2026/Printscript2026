package cli.io

import java.io.File

/** Un [Source] respaldado por un archivo del file system del usuario. */
class FileSource(
    private val file: File,
) : Source {
    constructor(path: String) : this(File(path))

    override val name: String get() = file.name

    /**
     * [use] cierra el archivo al terminar, por lo que el flujo sólo es válido dentro del
     * [block]: el resultado se calcula ahí y recién después se libera el recurso.
     */
    override fun <T> useLines(block: (Sequence<String>) -> T): T = file.bufferedReader().use { reader -> block(reader.lineSequence()) }
}
