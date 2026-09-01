package cli.arguments

/**
 * Operación que la CLI puede ejecutar sobre un fuente.
 *
 * [requiresConfig] distingue a las herramientas configurables (Formatter y Linter, según la
 * consigna) de las que no lo son, para que el parseo de argumentos exija el archivo de
 * configuración sin necesitar un `when` por operación.
 */
enum class Operation(
    val argument: String,
    val requiresConfig: Boolean,
) {
    VALIDATION("validation", requiresConfig = false),
    EXECUTION("execution", requiresConfig = false),
    FORMATTING("formatting", requiresConfig = true),
    ANALYZING("analyzing", requiresConfig = true),
    ;

    companion object {
        fun fromArgument(argument: String): Operation? = entries.find { it.argument == argument.lowercase() }

        fun supportedArguments(): String = entries.joinToString(", ") { it.argument }
    }
}
