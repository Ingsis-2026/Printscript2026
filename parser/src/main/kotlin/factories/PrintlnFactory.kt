package factories

import ast.ASTNode
import ast.PrintNode
import parser.ExpressionParser
import parser.ParserException
import parser.closesParenthesis
import parser.namesFunction
import parser.opensParenthesis
import token.Token

class PrintlnFactory : ASTFactory {
    override fun createAST(tokens: List<Token>): ASTNode {
        if (tokens.size < MIN_PRINTLN_TOKENS) {
            throw ParserException("Invalid token structure for println: Too few tokens", tokens)
        }

        val openParenIndex = tokens.indexOfFirst { it.opensParenthesis }
        val closeParenIndex = tokens.indexOfLast { it.closesParenthesis }

        if (openParenIndex == -1 || closeParenIndex == -1 || openParenIndex >= closeParenIndex) {
            throw ParserException("Invalid token structure for println: Missing or misordered parentheses", tokens)
        }

        val expressionTokens = tokens.subList(openParenIndex + 1, closeParenIndex)

        if (expressionTokens.isEmpty()) {
            throw ParserException("Cannot create AST for println from an empty expression", tokens)
        }

        return PrintNode(ExpressionParser.parse(expressionTokens), tokens[0].getPosition())
    }

    override fun canHandle(tokens: List<Token>): Boolean = tokens.firstOrNull()?.let { it.namesFunction && it.value == "println" } == true

    private companion object {
        /** `println` + `(` + expresión + `)` es la estructura mínima válida. */
        const val MIN_PRINTLN_TOKENS = 4
    }
}
