package factories

import ast.ASTNode
import ast.BinaryNode
import ast.LiteralNode
import parser.ParserException
import parser.closesParenthesis
import parser.isOperator
import parser.opensParenthesis
import token.Token

class OperationFactory {
    fun createAST(listTokens: List<Token>): ASTNode {
        if (listTokens.size == 1) {
            return createLiteralNode(listTokens[0])
        }
        val tokens =
            if (listTokens.first().opensParenthesis && listTokens.last().closesParenthesis) {
                removeFirstAndLastParentheses(
                    listTokens,
                )
            } else {
                listTokens
            }
        return splitOnOperator(tokens, ::isAdditionOrSubtraction)
            ?: splitOnOperator(tokens, ::isMultiplicationOrDivision)
            ?: throw ParserException(
                "Error in operation",
                tokens.firstOrNull()?.getPosition(),
                tokens.lastOrNull()?.getFinalPosition(),
            )
    }

    /**
     * Parte la lista por el primer token que cumpla [isOperator] fuera de todo
     * paréntesis, y construye el nodo binario correspondiente. Devuelve `null`
     * si no hay ningún operador de ese tipo en el nivel más externo.
     */
    private fun splitOnOperator(
        tokens: List<Token>,
        isOperator: (Token) -> Boolean,
    ): ASTNode? {
        val openParentheses = emptyList<Token>().toMutableList()
        for (token in tokens) {
            if (token.opensParenthesis) openParentheses.add(token)
            if (token.closesParenthesis) openParentheses.removeLast()
            if (isOperator(token) && openParentheses.isEmpty()) {
                return BinaryNode(
                    left = createAST(tokens.subList(0, tokens.indexOf(token))),
                    right = createAST(tokens.subList(tokens.indexOf(token) + 1, tokens.size)),
                    operator = token,
                    position = token.getPosition(),
                )
            }
        }
        return null
    }

    private fun removeFirstAndLastParentheses(tokens: List<Token>): List<Token> {
        val parentheses = emptyList<String>().toMutableList()
        if (tokens.first().opensParenthesis) parentheses.add("(")
        for (token in tokens.subList(1, tokens.size - 1)) {
            if (token.opensParenthesis) parentheses.add("(")
            if (token.closesParenthesis) parentheses.removeLast()
            if (parentheses.isEmpty()) return tokens
        }
        return tokens.subList(1, tokens.size - 1)
    }

    private fun createLiteralNode(token: Token): ASTNode =
        LiteralNode(value = token.value, type = token.getType(), position = token.getPosition())

    private fun isMultiplicationOrDivision(token: Token) = token.isOperator("*") || token.isOperator("/")

    private fun isAdditionOrSubtraction(token: Token) = token.isOperator("+") || token.isOperator("-")
}
