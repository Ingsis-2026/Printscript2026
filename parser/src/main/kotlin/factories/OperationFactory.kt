package factories

import ast.ASTNode
import ast.BinaryNode
import ast.LiteralNode
import token.Token

class OperationFactory {
    fun createAST(listTokens: List<Token>): ASTNode {
        if (listTokens.size == 1) {
            return createLiteralNode(listTokens[0])
        }
        val tokens =
            if (listTokens.first().value == "(" && listTokens.last().value == ")") {
                removeFirstAndLastParentheses(
                    listTokens,
                )
            } else {
                listTokens
            }
        val parenthesis1 = emptyList<Token>().toMutableList()
        for (token in tokens) {
            if (token.value == "(") parenthesis1.add(token)
            if (token.value == ")") parenthesis1.removeLast()
            if (isAdditionOrSubtraction(token) && parenthesis1.isEmpty()) {
                return BinaryNode(
                    left = createAST(tokens.subList(0, tokens.indexOf(token))),
                    right = createAST(tokens.subList(tokens.indexOf(token) + 1, tokens.size)),
                    operator = token,
                    position = token.getPosition(),
                )
            }
        }
        val parenthesis2 = emptyList<Token>().toMutableList()
        for (token in tokens) {
            if (token.value == "(") parenthesis2.add(token)
            if (token.value == ")") parenthesis2.removeLast()
            if (isMultiplicationOrDivision(token) && parenthesis2.isEmpty()) {
                return BinaryNode(
                    left = createAST(tokens.subList(0, tokens.indexOf(token))),
                    right = createAST(tokens.subList(tokens.indexOf(token) + 1, tokens.size)),
                    operator = token,
                    position = token.getPosition(),
                )
            }
        }
        throw Exception("Error in operation")
    }

    private fun removeFirstAndLastParentheses(tokens: List<Token>): List<Token> {
        val parentheses = emptyList<String>().toMutableList()
        if (tokens.first().value == "(") parentheses.add("(")
        for (token in tokens.subList(1, tokens.size - 1)) {
            if (token.value == "(") parentheses.add("(")
            if (token.value == ")") parentheses.removeLast()
            if (parentheses.isEmpty()) return tokens
        }
        return tokens.subList(1, tokens.size - 1)
    }

    private fun createLiteralNode(token: Token): ASTNode =
        LiteralNode(value = token.value, type = token.getType(), position = token.getPosition())

    private fun isMultiplicationOrDivision(token: Token) = token.value == "*" || token.value == "/"

    private fun isAdditionOrSubtraction(token: Token) = token.value == "+" || token.value == "-"
}
