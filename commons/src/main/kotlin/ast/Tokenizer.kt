package ast

import token.Token
import token.TokenPosition
import token.TokenType

class Tokenizer {
    /** Variante perezosa: re-tokeniza cada nodo a medida que se lo pide. */
    fun parseToTokens(astNodes: Sequence<ASTNode>): Sequence<List<Token>> = astNodes.map { extractTokensFromAST(it) }

    fun parseToTokens(astNodes: List<ASTNode>): List<List<Token>> = parseToTokens(astNodes.asSequence()).toList()

    private fun extractTokensFromAST(node: ASTNode): List<Token> {
        val tokens = mutableListOf<Token>()
        traverseAST(
            node,
            tokens,
        )
        return tokens
    }

    private fun traverseAST(
        node: ASTNode,
        tokens: MutableList<Token>,
    ) {
        when (node) {
            is LiteralNode -> tokens.add(Token(node.type, node.value, node.position, node.position))
            is BinaryNode -> traverseBinary(node, tokens)
            is PrintNode -> traversePrint(node, tokens)
            is DeclarationNode -> traverseNamed(node.id, node.position, node.expr, tokens)
            is AssignationNode -> traverseNamed(node.id, node.position, node.expression, tokens)
            is BlockNode -> node.nodes.forEach { traverseAST(it, tokens) }
            is ConditionalNode -> traverseConditional(node, tokens)
            is FunctionNode -> traverseFunction(node, tokens)
            is NilNode -> Unit
        }
    }

    private fun traverseBinary(
        node: BinaryNode,
        tokens: MutableList<Token>,
    ) {
        traverseAST(node.left, tokens)
        tokens.add(Token(TokenType.OPERATOR, node.operator.value, node.position, node.position))
        traverseAST(node.right, tokens)
    }

    private fun traversePrint(
        node: PrintNode,
        tokens: MutableList<Token>,
    ) {
        // "println" es la palabra real del lenguaje; no existe un "print" en PrintScript.
        tokens.add(Token(TokenType.FUNCTION, "println", node.position, node.position))
        traverseAST(node.expression, tokens)
    }

    /** Declaraciones y asignaciones comparten forma: identificador seguido de su expresión. */
    private fun traverseNamed(
        id: String,
        position: TokenPosition,
        expression: ASTNode,
        tokens: MutableList<Token>,
    ) {
        tokens.add(Token(TokenType.IDENTIFIER, id, position, position))
        traverseAST(expression, tokens)
    }

    private fun traverseConditional(
        node: ConditionalNode,
        tokens: MutableList<Token>,
    ) {
        tokens.add(Token(TokenType.CONDITIONAL, "if", node.position, node.position))
        traverseAST(node.condition, tokens)
        traverseAST(node.thenBlock, tokens)

        node.elseBlock?.let {
            tokens.add(Token(TokenType.CONDITIONAL, "else", it.position, it.position))
            traverseAST(it, tokens)
        }
    }

    private fun traverseFunction(
        node: FunctionNode,
        tokens: MutableList<Token>,
    ) {
        tokens.add(Token(TokenType.FUNCTION, node.functionName.lowercase(), node.position, node.position))
        traverseAST(node.expression, tokens)
    }
}
