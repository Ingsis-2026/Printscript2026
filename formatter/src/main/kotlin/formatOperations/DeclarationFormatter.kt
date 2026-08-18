package formatOperations

import ast.ASTNode
import ast.DeclarationNode
import formatOperations.commons.HandleSpace
import formatter.Formatter

class DeclarationFormatter(
    private val allowedDeclarationKeywords: List<String>,
    private val allowedDataTypes: List<String>,
) : FormattingOperation {
    private val handleSpace: HandleSpace = HandleSpace()

    override fun canHandle(astNode: ASTNode): Boolean {
        return astNode is DeclarationNode
    }

    override fun format(
        node: ASTNode,
        formatter: Formatter,
    ): String {
        if (!canHandle(node)) error("Node isn't a DeclarationNode")
        val declarationNode = node as DeclarationNode
        val declKeywordValue =
            if (allowedDeclarationKeyword(
                    declarationNode.declValue,
                )
            ) {
                declarationNode.declValue
            } else {
                throw UnsupportedOperationException(
                    "Unsupported declaration type ${declarationNode.declValue}",
                )
            }
        val id = declarationNode.id

        val formatOperationsList = listOf(LiteralFormatter(), BinaryFormatter())
        val exprValue =
            formatOperationsList.find { it -> it.canHandle(declarationNode.expr) }
                ?.format(declarationNode.expr, formatter)

        val dataType =
            if (allowedDataType(declarationNode.dataTypeValue)) {
                declarationNode.dataTypeValue
            } else {
                throw UnsupportedOperationException(
                    "Unsupported data type ${declarationNode.dataTypeValue}",
                )
            }

        val spaceBeforeColon = formatter.getRules()["spaceBeforeColon"] as Boolean
        val spaceAfterColon = formatter.getRules()["spaceAfterColon"] as Boolean
        val spaceAroundEquals = formatter.getRules()["spaceAroundEquals"] as Boolean

        val equal = handleSpace.handleSpace("=", spaceAroundEquals, spaceAroundEquals)
        val colon = handleSpace.handleSpace(":", spaceBeforeColon, spaceAfterColon)

        return "$declKeywordValue $id$colon$dataType$equal$exprValue"
    }

    private fun allowedDeclarationKeyword(declKeyword: String): Boolean {
        return allowedDeclarationKeywords.contains(
            declKeyword,
        )
    }

    private fun allowedDataType(dataType: String): Boolean {
        return allowedDataTypes.contains(
            dataType,
        )
    }
}
