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

    override fun toString(): String =
        "Token(type = '$type', value = '$value', " +
            "start = '$initialPosition', end = '$finalPosition')"
}
