package cli.io

/**
 * El fuente a procesar, entregado siempre de forma perezosa.
 *
 * La consigna advierte que un fuente puede no caber en memoria, así que la abstracción nunca
 * expone su contenido completo: sólo un flujo de líneas, válido mientras dure el bloque que lo
 * consume, para que el implementador pueda cerrar el recurso al terminar.
 */
interface Source {
    /** Nombre legible del fuente, para los mensajes de la CLI. */
    val name: String

    fun <T> useLines(block: (Sequence<String>) -> T): T
}
