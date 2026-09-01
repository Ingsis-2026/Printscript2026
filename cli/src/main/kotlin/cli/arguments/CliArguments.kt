package cli.arguments

/** Pedido ya validado: qué operación correr, sobre qué fuente y con qué configuración. */
data class CliArguments(
    val operation: Operation,
    val sourcePath: String,
    val version: String,
    val configPath: String? = null,
    val outputPath: String? = null,
)
