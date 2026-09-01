package token

/**
 * Los campos son inmutables: [Token] participa en `equals`/`hashCode` y se usa como
 * contenido de listas comparadas por igualdad, por lo que mutarlo rompería esas comparaciones.
 */
data class Token(
    private val type: TokenType,
    val value: String,
    private val initialPosition: TokenPosition,
    private val finalPosition: TokenPosition,
) {
    fun getType(): TokenType = type

    fun getPosition(): TokenPosition = initialPosition

    /**
     * Posición donde termina el token. La consigna pide informar inicio y fin de cada
     * problema, y hasta ahora este dato se guardaba sin forma de leerlo.
     */
    fun getFinalPosition(): TokenPosition = finalPosition

    override fun toString(): String =
        "Token(type = '$type', value = '$value', " +
            "start = '$initialPosition', end = '$finalPosition')"
}
