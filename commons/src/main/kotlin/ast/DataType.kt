package ast

/** Los tipos de dato de PrintScript, cada uno con la palabra que lo nombra en una declaración. */
enum class DataType(
    val keyword: String,
) {
    NUMBER("number"),
    STRING("string"),
    BOOLEAN("boolean"),
    ;

    companion object {
        fun named(keyword: String): DataType? = entries.find { it.keyword == keyword }
    }
}
