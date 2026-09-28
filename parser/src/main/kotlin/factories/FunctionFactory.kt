package factories

import ast.ASTNode
import parser.ExpressionParser
import parser.namesFunction
import token.Token

/** Una llamada que es la sentencia entera, como `readInput("nombre?");`. */
class FunctionFactory : ASTFactory {
    override fun createAST(tokens: List<Token>): ASTNode = ExpressionParser.parse(tokens)

    override fun canHandle(tokens: List<Token>): Boolean = tokens.any { it.namesFunction }
}
