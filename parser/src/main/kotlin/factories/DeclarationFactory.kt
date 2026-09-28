package factories

import ast.ASTNode
import ast.DataType
import ast.DeclarationNode
import ast.NilNode
import parser.ExpressionParser
import parser.ParserException
import parser.namesFunction
import token.Token
import token.TokenType

class DeclarationFactory : ASTFactory {
    override fun createAST(tokens: List<Token>): ASTNode {
        val keywordToken =
            tokens.find { it.getType() == TokenType.KEYWORD }
                ?: throw ParserException("Expected a KEYWORD token but found none.", tokens)
        val identifierToken =
            tokens.find { it.getType() == TokenType.IDENTIFIER }
                ?: throw ParserException("Expected an IDENTIFIER token but found none.", tokens)
        val dataTypeToken =
            tokens.find { it.getType() == TokenType.DATA_TYPE }
                ?: throw ParserException("Expected a DATA_TYPE token but found none.", tokens)
        val dataType =
            DataType.named(dataTypeToken.value)
                ?: throw ParserException("Unknown data type ${dataTypeToken.value}", tokens)

        val expressionTokens = expressionTokensOf(tokens)
        val expression = expressionTokens?.let { ExpressionParser.parse(it) } ?: NilNode

        if (expressionTokens != null && expressionTokens.none { it.namesFunction }) {
            rejectLiteralsOfAnotherType(dataType, expressionTokens)
        }

        return DeclarationNode(
            declType = keywordToken.getType(),
            declValue = keywordToken.value,
            id = identifierToken.value,
            dataType = dataType,
            expr = expression,
            position = identifierToken.getPosition(),
        )
    }

    override fun canHandle(tokens: List<Token>): Boolean = tokens.any { it.getType() == TokenType.KEYWORD }

    /** Lo que sigue al `=`, o `null` si la declaración no trae valor: `let x: number;`. */
    private fun expressionTokensOf(tokens: List<Token>): List<Token>? {
        val assignationIndex = tokens.indexOfFirst { it.getType() == TokenType.ASSIGNATION }
        if (assignationIndex < 0) return null
        return tokens.subList(assignationIndex + 1, tokens.size).ifEmpty {
            throw ParserException("Expected an expression after =", tokens)
        }
    }

    /** Un string admite números y booleanos concatenados; los otros tipos, sólo literales propios. */
    private fun rejectLiteralsOfAnotherType(
        dataType: DataType,
        expressionTokens: List<Token>,
    ) {
        val literalTypes = expressionTokens.mapNotNull { literalTypeOf(it) }.toSet()
        val hasLiteralsOfAnotherType = (literalTypes - dataType).isNotEmpty()
        val concatenatesIntoString = dataType == DataType.STRING && DataType.STRING in literalTypes
        if (hasLiteralsOfAnotherType && !concatenatesIntoString) {
            throw ParserException("declared data type ${dataType.keyword} is inconsistent with the expression", expressionTokens)
        }
    }

    private fun literalTypeOf(token: Token): DataType? =
        when (token.getType()) {
            TokenType.NUMBERLITERAL -> DataType.NUMBER
            TokenType.STRINGLITERAL -> DataType.STRING
            TokenType.BOOLEANLITERAL -> DataType.BOOLEAN
            else -> null
        }
}
