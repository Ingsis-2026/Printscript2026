package factories

import ast.ASTNode
import ast.BlockNode
import ast.ConditionalNode
import ast.LiteralNode
import ast.NilNode
import parser.Parser
import token.Token
import token.TokenType

class ConditionalFactory(
    private val subParserProvider: () -> Parser = { Parser() },
) : ASTFactory {
    override fun createAST(tokens: List<Token>): ASTNode {
        val startCondition = tokens.indexOfFirst { it.value == "(" } + 1
        val conditionToken = tokens[startCondition]
        val conditionNode =
            LiteralNode(
                value = conditionToken.value,
                type = conditionToken.getType(),
                position = conditionToken.getPosition(),
            )

        val thenBlock = parseBlock(tokens, "if")

        val elseBlock =
            if (tokens.any { it.value == "else" }) {
                parseBlock(tokens, "else")
            } else {
                NilNode
            }

        return ConditionalNode(
            condition = conditionNode,
            thenBlock = thenBlock ?: NilNode,
            elseBlock = elseBlock ?: NilNode,
            position = conditionNode.position,
        )
    }

    private fun parseBlock(
        allTokens: List<Token>,
        blockType: String,
    ): ASTNode? {
        val index = allTokens.indexOfFirst { it.value == blockType }
        if (index < 0) return null
        val tokens = allTokens.drop(index)

        val startBlock = tokens.indexOfFirst { it.getType() == TokenType.PUNCTUATOR && it.value == "{" }
        val endBlock = tokens.indexOfFirst { it.getType() == TokenType.PUNCTUATOR && it.value == "}" }

        if (startBlock < 0 || endBlock < 0 || endBlock <= startBlock) {
            throw RuntimeException("Error parsing block: missing or unbalanced braces")
        }

        return BlockNode(
            nodes = subParserProvider().execute(tokens.subList(startBlock + 1, endBlock + 1)),
            position = tokens[startBlock + 1].getPosition(),
        )
    }

    override fun canHandle(tokens: List<Token>): Boolean {
        return tokens.any { it.getType() == TokenType.CONDITIONAL && it.value == "if" }
    }
}