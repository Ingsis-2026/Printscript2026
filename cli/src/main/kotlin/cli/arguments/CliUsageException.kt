package cli.arguments

/**
 * Error de uso de la CLI: los argumentos recibidos no describen un pedido ejecutable.
 *
 * Se distingue de los errores de PrintScript porque no habla del programa del usuario sino de
 * cómo se invocó la herramienta, y por eso la CLI lo reporta junto con la línea de uso.
 */
class CliUsageException(
    message: String,
) : RuntimeException(message)
