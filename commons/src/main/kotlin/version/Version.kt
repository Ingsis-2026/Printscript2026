package version

/**
 * Las versiones de PrintScript, de la más vieja a la más nueva.
 *
 * Cada módulo dice qué hace en cada versión con un `when` exhaustivo sobre este tipo, así que una
 * versión nueva deja de compilar en todos los módulos hasta que cada uno diga qué agrega.
 */
enum class Version(
    val number: String,
) {
    V1_0("1.0"),
    V1_1("1.1"),
    ;

    companion object {
        fun of(number: String): Version? = entries.find { it.number == number }

        /** Para quien recibe la versión como texto y no tiene cómo rechazarla antes, como el TCK. */
        fun parse(number: String): Version = of(number) ?: throw IllegalArgumentException("Unsupported version: $number")
    }
}
