package factories

import ast.ASTNode
import ast.AssignationNode
import ast.LiteralNode
import ast.NilNode
import token.Token
import token.TokenType

class AssignationFactory : ASTFactory {
    override fun createAST(tokens: List<Token>): ASTNode {
        val assignationToken =
            tokens.find { it.getType() == TokenType.ASSIGNATION }
                ?: throw Exception("Assignation token not found")

        val leftTokens = getLeftTokens(tokens)
        val rightTokens = getRightTokens(tokens, leftTokens)

        val leftNode =
            when {
                leftTokens.size > 1 -> variableDeclaration(leftTokens)
                leftTokens.isNotEmpty() -> createLiteralNode(leftTokens[0])
                else -> throw Exception("Invalid left side of assignment")
            }

        val rightNode =
            when {
                rightTokens.isEmpty() -> NilNode
                rightTokens.size > 1 -> createAssignedTree(rightTokens)
                else -> createLiteralNode(rightTokens[0])
            }

        val leftNodeId =
            if (leftNode is LiteralNode) {
                leftNode.value
            } else {
                throw Exception("Left side of assignment must be a literal or identifier")
            }

        return AssignationNode(
            id = leftNodeId,
            expression = rightNode,
            valType = assignationToken.getType(),
            position = assignationToken.getPosition(),
        )
    }

    private fun createLiteralNode(token: Token): ASTNode {
        return LiteralNode(
            value = token.value,
            type = token.getType(),
            position = token.getPosition(),
        )
    }

    private fun getRightTokens(
        tokens: List<Token>,
        leftTokens: List<Token>,
    ) = tokens.drop(leftTokens.size + 1)

    private fun getLeftTokens(tokens: List<Token>) = tokens.takeWhile { it.getType() != TokenType.ASSIGNATION }

    private fun createAssignedTree(tokens: List<Token>): ASTNode {
        return if (tokens.any { it.getType() == TokenType.FUNCTION }) {
            FunctionFactory().createAST(tokens)
        } else {
            OperationFactory().createAST(tokens)
        }
    }

    private fun variableDeclaration(tokens: List<Token>): ASTNode {
        return DeclarationFactory().createAST(tokens)
    }

    override fun canHandle(tokens: List<Token>): Boolean {
        return tokens.any { it.getType() == TokenType.ASSIGNATION }
    }
}