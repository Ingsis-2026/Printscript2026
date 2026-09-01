package parser

import ast.ASTNode
import factories.ASTFactory
import token.Token

class Parser(
    private val factories: List<ASTFactory> = ParserFactory.defaultFactories(),
) {
    companion object {
        fun forVersion(version: String): Parser = ParserFactory.forVersion(version)
    }

    fun execute(tokens: List<Token>): List<ASTNode> {
        val result = mutableListOf<ASTNode>()
        val sameLineTokens = StatementSplitter().split(tokens)
        for (tokenList in sameLineTokens) {
            val astFactory = determineFactory(tokenList)
            if (astFactory != null) {
                result.add(astFactory.createAST(tokenList))
            } else {
                throw ParserException("Can't handle this sentence")
            }
        }
        return result
    }

    private fun determineFactory(tokens: List<Token>): ASTFactory? = factories.find { it.canHandle(tokens) }
}
