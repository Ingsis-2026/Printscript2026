package cli.arguments

import version.Version

/** Pedido ya validado: qué operación correr, sobre qué fuente y con qué configuración. */
data class CliArguments(
    val operation: Operation,
    val sourcePath: String,
    val version: Version,
    val configPath: String? = null,
    val outputPath: String? = null,
)
