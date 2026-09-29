package formatter

/**
 * Qué clase de sentencia es, según su primer token. Es todo lo que las reglas preguntan de una
 * sentencia: los ":" sólo se espacian en una declaración, y los saltos de println sólo siguen a
 * un println.
 */
internal enum class StatementKind {
    DECLARATION,
    PRINTLN,
    OTHER,
    ;

    companion object {
        fun startedBy(token: SourceToken): StatementKind =
            when (token.value) {
                "let", "const" -> DECLARATION
                "println" -> PRINTLN
                else -> OTHER
            }
    }
}
