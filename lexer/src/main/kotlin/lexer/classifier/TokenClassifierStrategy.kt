package lexer.classifier

interface TokenClassifierStrategy {
    fun classify(tokenValue: String): Boolean
}
