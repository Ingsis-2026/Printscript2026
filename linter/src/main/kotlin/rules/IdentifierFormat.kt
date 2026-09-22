package rules

/** Las formas de escribir un identificador que el linter sabe exigir. */
enum class IdentifierFormat(
    val message: String,
) {
    /** Empieza en minúscula y no usa guiones bajos como separador. */
    CAMEL_CASE("The following identifier must be in camel case") {
        override fun matches(identifier: String): Boolean = identifier.startsLowerCase() && !identifier.contains("_")
    },

    /** Empieza en minúscula y separa las palabras con un único guión bajo. */
    SNAKE_CASE("The following identifier must be in snake case") {
        override fun matches(identifier: String): Boolean =
            identifier.startsLowerCase() &&
                !identifier.contains("__") &&
                identifier.all { it.isLowerCase() || it == '_' }
    },
    ;

    abstract fun matches(identifier: String): Boolean
}

private fun String.startsLowerCase(): Boolean = isNotEmpty() && this[0].isLowerCase()
