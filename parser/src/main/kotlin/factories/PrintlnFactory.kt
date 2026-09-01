package factories

import ast.ASTNode
import ast.PrintNode
import parser.ParserException
import token.Token
import token.TokenType

class PrintlnFactory : ASTFactory {
    override fun createAST(tokens: List<Token>): ASTNode {
        if (tokens.size < MIN_PRINTLN_TOKENS) {
            throw error(tokens, "Invalid token structure for println: Too few tokens")
        }

        val openParenIndex = tokens.indexOfFirst { it.getType() == TokenType.PARENTHESIS && it.value == "(" }
        val closeParenIndex = tokens.indexOfLast { it.getType() == TokenType.PARENTHESIS && it.value == ")" }

        if (openParenIndex == -1 || closeParenIndex == -1 || openParenIndex >= closeParenIndex) {
            throw error(tokens, "Invalid token structure for println: Missing or misordered parentheses")
        }

        val expressionTokens = tokens.subList(openParenIndex + 1, closeParenIndex)

        if (expressionTokens.isEmpty()) {
            throw error(tokens, "Cannot create AST for println from an empty expression")
        }

        val expressionNode = OperationFactory().createAST(expressionTokens)
        return PrintNode(expressionNode, tokens[0].getPosition())
    }

    private fun error(
        tokens: List<Token>,
        message: String,
    ) = ParserException(
        message,
        tokens.firstOrNull()?.getPosition(),
        tokens.lastOrNull()?.getFinalPosition(),
    )

    override fun canHandle(tokens: List<Token>): Boolean =
        tokens.isNotEmpty() && tokens[0].getType() == TokenType.FUNCTION && tokens[0].value == "println"

    private companion object {
        /** `println` + `(` + expresión + `)` es la estructura mínima válida. */
        const val MIN_PRINTLN_TOKENS = 4
    }
}
