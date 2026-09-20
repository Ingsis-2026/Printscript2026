package lexer

/** Reconoce un tipo de token: sabe responder si un texto es, entero, una ocurrencia de su [regex]. */
class RegexTokenClassifier(
    val regex: Regex,
) {
    fun classify(tokenValue: String): Boolean = regex.matches(tokenValue)
}
