package cli.io

/**
 * Salida de la CLI, separada en tres canales por destino.
 *
 * Existe como interfaz para que los comandos no escriban a `stdout` directamente: así los
 * tests pueden capturar lo que se informó y afirmar sobre el contenido.
 */
interface Output {
    /** Mensajes de avance dirigidos a la persona (no forman parte del resultado). */
    fun info(message: String)

    /** El resultado de la operación: lo único que se puede canalizar a otro proceso. */
    fun result(message: String)

    /** Errores. */
    fun error(message: String)
}
