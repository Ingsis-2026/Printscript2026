package factories

import ast.ASTNode
import token.Token

interface ASTFactory {
    fun createAST(tokens: List<Token>): ASTNode

    fun canHandle(tokens: List<Token>): Boolean
}
