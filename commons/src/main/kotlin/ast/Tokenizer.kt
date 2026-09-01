package ast

import token.Token
import token.TokenType

class Tokenizer {
    fun parseToTokens(astNodes: List<ASTNode>): List<List<Token>> {
        val tokens = mutableListOf<List<Token>>()
        for (node in astNodes) {
            val extractedTokens = extractTokensFromAST(node)
            println("Tokens from AST Node: $extractedTokens")
            tokens.add(extractedTokens)
        }
        return tokens
    }

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
            is LiteralNode -> traverseLiteral(node, tokens)
            is BinaryNode -> traverseBinary(node, tokens)
            is PrintNode -> traversePrint(node, tokens)
            is DeclarationNode -> traverseDeclaration(node, tokens)
            is AssignationNode -> traverseAssignation(node, tokens)
            is BlockNode -> node.nodes.forEach { traverseAST(it, tokens) }
            is ConditionalNode -> traverseConditional(node, tokens)
            is FunctionNode -> traverseFunction(node, tokens)
            is NilNode -> Unit
        }
    }

    private fun traverseLiteral(
        node: LiteralNode,
        tokens: MutableList<Token>,
    ) {
        tokens.add(
            Token(
                type = node.type,
                value = node.value,
                initialPosition = node.position,
                finalPosition = node.position,
            ),
        )
    }

    private fun traverseBinary(
        node: BinaryNode,
        tokens: MutableList<Token>,
    ) {
        traverseAST(node.left, tokens)
        tokens.add(
            Token(
                type = TokenType.OPERATOR,
                value = node.operator.value,
                initialPosition = node.position,
                finalPosition = node.position,
            ),
        )
        traverseAST(node.right, tokens)
    }

    private fun traversePrint(
        node: PrintNode,
        tokens: MutableList<Token>,
    ) {
        tokens.add(
            Token(
                type = TokenType.FUNCTION,
                value = "print",
                initialPosition = node.position,
                finalPosition = node.position,
            ),
        )
        traverseAST(node.expression, tokens)
    }

    private fun traverseDeclaration(
        node: DeclarationNode,
        tokens: MutableList<Token>,
    ) {
        tokens.add(Token(TokenType.IDENTIFIER, node.id, node.position, node.position))
        traverseAST(node.expr, tokens)
    }

    private fun traverseAssignation(
        node: AssignationNode,
        tokens: MutableList<Token>,
    ) {
        tokens.add(Token(TokenType.IDENTIFIER, node.id, node.position, node.position))
        traverseAST(node.expression, tokens)
    }

    private fun traverseConditional(
        node: ConditionalNode,
        tokens: MutableList<Token>,
    ) {
        tokens.add(
            Token(
                type = TokenType.CONDITIONAL,
                value = "if",
                initialPosition = node.position,
                finalPosition = node.position,
            ),
        )
        traverseAST(node.condition, tokens)
        traverseAST(node.thenBlock, tokens)

        node.elseBlock?.let {
            tokens.add(
                Token(
                    type = TokenType.CONDITIONAL,
                    value = "else",
                    initialPosition = it.position,
                    finalPosition = it.position,
                ),
            )
            traverseAST(it, tokens)
        }
    }

    private fun traverseFunction(
        node: FunctionNode,
        tokens: MutableList<Token>,
    ) {
        tokens.add(
            Token(
                type = TokenType.FUNCTION,
                value = node.functionName.lowercase(),
                initialPosition = node.position,
                finalPosition = node.position,
            ),
        )
        traverseAST(node.expression, tokens)
    }
}
