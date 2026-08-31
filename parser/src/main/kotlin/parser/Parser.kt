package parser

import ast.ASTNode
import factories.ASTFactory
import factories.AssignationFactory
import factories.ConditionalFactory
import factories.DeclarationFactory
import factories.FunctionFactory
import factories.PrintlnFactory
import token.Token

class Parser(
    private val factories: List<ASTFactory> = defaultFactories(),
) {
    companion object {
        fun defaultFactories(): List<ASTFactory> =
            listOf(
                ConditionalFactory(),
                PrintlnFactory(),
                DeclarationFactory(),
                AssignationFactory(),
                FunctionFactory(),
            )

        fun forVersion(version: String): Parser =
            when (version) {
                "1.0" ->
                    Parser(
                        listOf(
                            PrintlnFactory(),
                            DeclarationFactory(),
                            AssignationFactory(),
                            FunctionFactory(),
                        ),
                    )
                "1.1" ->
                    Parser(
                        listOf(
                            ConditionalFactory(),
                            PrintlnFactory(),
                            DeclarationFactory(),
                            AssignationFactory(),
                            FunctionFactory(),
                        ),
                    )
                else -> throw IllegalArgumentException("Unsupported version: $version")
            }
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
