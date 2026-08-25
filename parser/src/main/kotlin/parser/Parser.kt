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
        val sameLineTokens = getSameLineTokens(tokens)
        for (tokenList in sameLineTokens) {
            val astFactory = determineFactory(tokenList)
            if (astFactory != null) {
                result.add(astFactory.createAST(tokenList))
            } else {
                throw Exception("Can't handle this sentence")
            }
        }
        return result
    }

    private fun determineFactory(tokens: List<Token>): ASTFactory? = factories.find { it.canHandle(tokens) }

    private fun getSameLineTokens(tokenList: List<Token>): List<List<Token>> {
        val rows = mutableListOf<List<Token>>()
        var singleRow = mutableListOf<Token>()
        var llaves = 0
        var expectingElse = false

        fun addRowToRows(
            row: List<Token>,
            lastToken: Token,
        ) {
            if (lastToken.value != ";" && lastToken.value != "}" && lastToken.value != "{") {
                throw Exception(
                    "las sentencias deben finalizar con \";\", \"}\" o \"{\"",
                )
            }
            rows.add(row)
        }

        for (i in tokenList.indices) {
            val token = tokenList[i]
            // Open a block

            when (token.value) {
                "{" -> {
                    llaves++
                    singleRow.add(token)
                }

                "}" -> {
                    llaves--
                    singleRow.add(token)
                    if (llaves == 0) {
                        if (i + 1 < tokenList.size && tokenList[i + 1].value == "else") {
                            expectingElse = true
                        } else {
                            addRowToRows(singleRow, token)
                            singleRow = mutableListOf()
                            expectingElse = false
                        }
                    }
                }
                "if" -> {
                    if (singleRow.isNotEmpty()) addRowToRows(singleRow, token)
                    singleRow.add(token)
                    expectingElse = true
                }
                "else" ->
                    if (expectingElse) {
                        singleRow.add(token)
                        expectingElse = false
                    } else {
                        continue
                    }
                ";" ->
                    if (llaves == 0 && singleRow.isNotEmpty()) {
                        addRowToRows(singleRow, token)
                        singleRow = mutableListOf()
                    } else {
                        continue
                    }
                else -> singleRow.add(token)
            }
        }

        if (singleRow.isNotEmpty()) addRowToRows(singleRow, singleRow.last())
        if (rows.isEmpty()) throw Exception("Error: No valid code.")

        return rows
    }
}
