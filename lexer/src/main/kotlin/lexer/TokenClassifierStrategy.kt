package lexer

interface TokenClassifierStrategy {
    fun classify(tokenValue: String): Boolean
}
