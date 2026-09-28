package factories

import ast.ASTNode
import ast.AssignationNode
import ast.NilNode
import parser.ExpressionParser
import parser.ParserException
import token.Token
import token.TokenType

class AssignationFactory : ASTFactory {
    override fun createAST(tokens: List<Token>): ASTNode {
        val assignationIndex = tokens.indexOfFirst { it.getType() == TokenType.ASSIGNATION }
        if (assignationIndex < 0) throw ParserException("Assignation token not found", tokens)
        val assignationToken = tokens[assignationIndex]

        val leftTokens = tokens.subList(0, assignationIndex)
        val rightTokens = tokens.subList(assignationIndex + 1, tokens.size)

        return AssignationNode(
            id = targetOf(leftTokens, tokens),
            expression = if (rightTokens.isEmpty()) NilNode else ExpressionParser.parse(rightTokens),
            valType = assignationToken.getType(),
            position = assignationToken.getPosition(),
        )
    }

    /**
     * El nombre que recibe el valor. Varios tokens antes del `=` sólo podrían ser una declaración,
     * y sin keyword no lo es: así se rechaza `const x: number = 1` en 1.0, donde `const` no existe.
     */
    private fun targetOf(
        leftTokens: List<Token>,
        tokens: List<Token>,
    ): String =
        when (leftTokens.size) {
            0 -> throw ParserException("Invalid left side of assignment", tokens)
            1 -> leftTokens.single().value
            else -> throw ParserException("Expected a KEYWORD token but found none.", leftTokens)
        }

    override fun canHandle(tokens: List<Token>): Boolean = tokens.any { it.getType() == TokenType.ASSIGNATION }
}
