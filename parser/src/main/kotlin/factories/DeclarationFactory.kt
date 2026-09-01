package factories

import ast.ASTNode
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

        val initialPositionExpression = tokens.indexOfFirst { it.value == "=" } + 1
        val expressionTokens: List<Token>? = findExpressionTokens(initialPositionExpression, tokens)

        val expressionNode: ASTNode = if (expressionTokens == null) NilNode else findExpressionNode(expressionTokens)
        val dataTypeValue = dataTypeToken.value

        // Una llamada a readInput/readEnv no se puede chequear acá: sus tokens son los del
        // argumento —un string— y no dicen nada del tipo que devuelve, que se resuelve contra
        // el tipo declarado recién al ejecutar. Chequearla rechazaba "let b: boolean = readInput(...)".
        if (expressionTokens != null && !isFunctionCall(expressionTokens)) {
            checkConsistencyOfExpressionWithDataType(dataTypeValue, expressionTokens)
        }

        return DeclarationNode(
            declType = keywordToken.getType(),
            declValue = keywordToken.value,
            id = identifierToken.value,
            dataType = dataTypeToken.getType(),
            dataTypeValue = dataTypeValue,
            expr = expressionNode,
            position = identifierToken.getPosition(),
            // Cambiado aquí  keywordToken.getPosition(),
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

    private fun checkConsistencyOfExpressionWithDataType(
        dataTypeValue: String,
        expressionTokens: List<Token>,
    ) {
        val isInconsistent =
            when (dataTypeValue) {
                "number" ->
                    expressionTokens.any {
                        it.getType() == TokenType.BOOLEANLITERAL || it.getType() == TokenType.STRINGLITERAL
                    }
                // Un "string" admite concatenar variables (sin literales a la vista) y también
                // mezclar con "number", porque el resultado sigue siendo "string". Sólo es
                // inconsistente si no hay ningún literal de texto y sí hay literales de otro tipo.
                "string" ->
                    expressionTokens.none { it.getType() == TokenType.STRINGLITERAL } &&
                        expressionTokens.any {
                            it.getType() == TokenType.NUMBERLITERAL || it.getType() == TokenType.BOOLEANLITERAL
                        }
                "boolean" ->
                    expressionTokens.any {
                        it.getType() == TokenType.NUMBERLITERAL || it.getType() == TokenType.STRINGLITERAL
                    }
                else -> false
            }
        if (isInconsistent) {
            throw error(
                expressionTokens,
                "declared data type $dataTypeValue is inconsistent with the expression",
            )
        }
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
