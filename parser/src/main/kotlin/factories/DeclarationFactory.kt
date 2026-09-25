package factories

import ast.ASTNode
import ast.DataType
import ast.DeclarationNode
import ast.LiteralNode
import ast.NilNode
import parser.ParserException
import token.Token
import token.TokenType

class DeclarationFactory : ASTFactory {
    override fun createAST(tokens: List<Token>): ASTNode {
        val keywordToken =
            tokens.find { it.getType() == TokenType.KEYWORD }
                ?: throw error(tokens, "Expected a KEYWORD token but found none.")
        val identifierToken =
            tokens.find { it.getType() == TokenType.IDENTIFIER }
                ?: throw error(tokens, "Expected an IDENTIFIER token but found none.")
        val dataTypeToken =
            tokens.find { it.getType() == TokenType.DATA_TYPE }
                ?: throw error(tokens, "Expected a DATA_TYPE or DATA_TYPE token but found none.")
        val dataType =
            DataType.named(dataTypeToken.value)
                ?: throw error(tokens, "Unknown data type ${dataTypeToken.value}")

        val initialPositionExpression = tokens.indexOfFirst { it.getType() == TokenType.ASSIGNATION } + 1
        val expressionTokens: List<Token>? = findExpressionTokens(initialPositionExpression, tokens)

        val expressionNode: ASTNode = if (expressionTokens == null) NilNode else findExpressionNode(expressionTokens)

        if (expressionTokens != null && !isFunctionCall(expressionTokens)) {
            rejectLiteralsOfAnotherType(dataType, expressionTokens)
        }

        return DeclarationNode(
            declType = keywordToken.getType(),
            declValue = keywordToken.value,
            id = identifierToken.value,
            dataType = dataType,
            expr = expressionNode,
            position = identifierToken.getPosition(),
        )
    }

    override fun canHandle(tokens: List<Token>): Boolean = tokens.any { it.getType() == TokenType.KEYWORD }

    /** Error de parseo ubicado en el tramo de tokens que lo provocó. */
    private fun error(
        tokens: List<Token>,
        message: String,
    ) = ParserException(
        message,
        tokens.firstOrNull()?.getPosition(),
        tokens.lastOrNull()?.getFinalPosition(),
    )

    /** Un string admite números y booleanos concatenados; los otros tipos, sólo literales propios. */
    private fun rejectLiteralsOfAnotherType(
        dataType: DataType,
        expressionTokens: List<Token>,
    ) {
        val literalTypes = expressionTokens.mapNotNull { literalTypeOf(it) }.toSet()
        val hasLiteralsOfAnotherType = (literalTypes - dataType).isNotEmpty()
        val concatenatesIntoString = dataType == DataType.STRING && DataType.STRING in literalTypes
        if (hasLiteralsOfAnotherType && !concatenatesIntoString) {
            throw error(expressionTokens, "declared data type ${dataType.keyword} is inconsistent with the expression")
        }
    }

    private fun literalTypeOf(token: Token): DataType? =
        when (token.getType()) {
            TokenType.NUMBERLITERAL -> DataType.NUMBER
            TokenType.STRINGLITERAL -> DataType.STRING
            TokenType.BOOLEANLITERAL -> DataType.BOOLEAN
            else -> null
        }

    private fun findExpressionTokens(
        initialPositionExpression: Int,
        tokens: List<Token>,
    ): List<Token>? {
        if (initialPositionExpression <= 0) return null
        return if (initialPositionExpression != tokens.size) {
            tokens.subList(
                initialPositionExpression,
                tokens.size,
            )
        } else {
            listOf(tokens.last())
        }
    }

    private fun isFunctionCall(expressionTokens: List<Token>): Boolean = FunctionFactory().canHandle(expressionTokens)

    private fun findExpressionNode(expressionTokens: List<Token>): ASTNode =
        if (expressionTokens.size == 1) {
            createLiteralNode(
                expressionTokens[0],
            )
        } else {
            if (isFunctionCall(expressionTokens)) {
                FunctionFactory().createAST(expressionTokens)
            } else {
                OperationFactory().createAST(expressionTokens)
            }
        }

    private fun createLiteralNode(token: Token): ASTNode =
        LiteralNode(
            value = token.value,
            type = token.getType(),
            position = token.getPosition(),
        )
}
