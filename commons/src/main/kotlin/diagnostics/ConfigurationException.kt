package diagnostics

/**
 * El archivo de configuración de una herramienta no se puede usar: está mal formado, o pide algo
 * que la versión elegida no tiene.
 *
 * Se distingue de [PrintScriptException] porque el problema no está en el programa del usuario
 * sino en cómo se configuró la herramienta, y por eso no lleva una ubicación en el fuente.
 */
class ConfigurationException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
