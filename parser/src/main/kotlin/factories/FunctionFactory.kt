package factories

import ast.ASTNode
import ast.FunctionNode
import ast.LiteralNode
import token.Token
import token.TokenType

class FunctionFactory : ASTFactory {
    override fun createAST(tokens: List<Token>): ASTNode {
        val indexFunctionToken = tokens.indexOfFirst { it.getType() == TokenType.FUNCTION }
        val functionToken = tokens.get(indexFunctionToken)
        val expressionToken = tokens.subList(indexFunctionToken + 2, tokens.size - 1)

        val expressionNode =
            LiteralNode(
                value = expressionToken.first().value,
                type = expressionToken.first().getType(),
                position = expressionToken.first().getPosition(),
            )

        return FunctionNode(
            type = functionToken.getType(),
            functionName = functionToken.value,
            expression = expressionNode,
            position = functionToken.getPosition(),
        )
    }

    override fun canHandle(tokens: List<Token>): Boolean = tokens.any { it.getType() == TokenType.FUNCTION }
}
