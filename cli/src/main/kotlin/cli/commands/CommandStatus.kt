package cli.commands

/**
 * Desenlace de una operación.
 *
 * Distingue "la herramienta funcionó" de "el fuente cumplió": el Analyzing termina bien
 * habiendo encontrado violaciones, y eso igual debe reflejarse en el código de salida.
 */
enum class CommandStatus {
    SUCCESS,
    FAILURE,
}
