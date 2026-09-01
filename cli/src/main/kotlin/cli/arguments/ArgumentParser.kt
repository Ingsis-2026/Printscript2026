package cli.arguments

import java.io.File

/**
 * Traduce los argumentos de la línea de comandos al pedido tipado que la CLI ejecuta.
 *
 * Valida acá todo lo que se puede saber sin abrir el fuente —operación conocida, versión
 * soportada, archivo existente, configuración presente cuando la operación la necesita— para
 * que los comandos reciban un [CliArguments] ya consistente y no tengan que revalidar nada.
 */
class ArgumentParser(
    private val supportedVersions: Set<String> = SUPPORTED_VERSIONS,
) {
    fun parse(arguments: List<String>): CliArguments {
        val (positional, flags) = split(arguments)

        val operation = readOperation(positional)
        val sourcePath = readSourcePath(positional)
        val version = flags[VERSION_FLAG] ?: DEFAULT_VERSION
        requireSupported(version)

        return CliArguments(
            operation = operation,
            sourcePath = sourcePath,
            version = version,
            configPath = readConfigPath(operation, flags),
            outputPath = flags[OUTPUT_FLAG],
        )
    }

    /**
     * Separa los argumentos en posicionales y opciones en un solo recorrido.
     *
     * Tiene que ser un recorrido único: el valor de una opción no es un argumento posicional, y
     * distinguirlos exige saber qué opción lo precede. Como efecto, un valor que empiece con
     * `--` tampoco se confunde con otra opción.
     */
    private fun split(arguments: List<String>): Pair<List<String>, Map<String, String>> {
        val positional = mutableListOf<String>()
        val flags = mutableMapOf<String, String>()
        var index = 0
        while (index < arguments.size) {
            val argument = arguments[index]
            if (argument.startsWith("--")) {
                requireKnown(argument)
                flags[argument] = readFlagValue(arguments, index)
                index += 2
            } else {
                positional.add(argument)
                index += 1
            }
        }
        return positional to flags
    }

    private fun readFlagValue(
        arguments: List<String>,
        flagIndex: Int,
    ): String =
        arguments.getOrNull(flagIndex + 1)
            ?: throw CliUsageException("La opción ${arguments[flagIndex]} necesita un valor.")

    private fun requireKnown(flag: String) {
        if (flag !in KNOWN_FLAGS) {
            throw CliUsageException("Opción desconocida: $flag. Opciones válidas: ${KNOWN_FLAGS.joinToString(", ")}.")
        }
    }

    private fun readOperation(positional: List<String>): Operation {
        val argument =
            positional.firstOrNull()
                ?: throw CliUsageException("Falta la operación a realizar. Operaciones válidas: ${Operation.supportedArguments()}.")
        return Operation.fromArgument(argument)
            ?: throw CliUsageException("Operación desconocida: $argument. Operaciones válidas: ${Operation.supportedArguments()}.")
    }

    private fun readSourcePath(positional: List<String>): String {
        val path =
            positional.getOrNull(1)
                ?: throw CliUsageException("Falta el archivo fuente.")
        val file = File(path)
        if (!file.exists()) throw CliUsageException("El archivo fuente no existe: $path")
        if (!file.isFile) throw CliUsageException("El archivo fuente no es un archivo: $path")
        return path
    }

    private fun readConfigPath(
        operation: Operation,
        flags: Map<String, String>,
    ): String? {
        val path = flags[CONFIG_FLAG]
        if (!operation.requiresConfig) return path
        if (path == null) {
            throw CliUsageException("La operación ${operation.argument} necesita un archivo de configuración ($CONFIG_FLAG).")
        }
        if (!File(path).isFile) throw CliUsageException("El archivo de configuración no existe: $path")
        return path
    }

    private fun requireSupported(version: String) {
        if (version !in supportedVersions) {
            throw CliUsageException("Versión no soportada: $version. Versiones válidas: ${supportedVersions.joinToString(", ")}.")
        }
    }

    companion object {
        const val DEFAULT_VERSION = "1.0"
        const val VERSION_FLAG = "--version"
        const val CONFIG_FLAG = "--config"
        const val OUTPUT_FLAG = "--output"

        val SUPPORTED_VERSIONS = setOf("1.0", "1.1")
        val KNOWN_FLAGS = listOf(VERSION_FLAG, CONFIG_FLAG, OUTPUT_FLAG)

        /** Línea de uso que la CLI muestra cuando la invocación no es válida. */
        fun usage(): String =
            "Uso: printscript <${Operation.supportedArguments().replace(", ", "|")}> <archivo> " +
                "[$VERSION_FLAG ${SUPPORTED_VERSIONS.joinToString("|")}] [$CONFIG_FLAG <archivo>] [$OUTPUT_FLAG <archivo>]"
    }
}
