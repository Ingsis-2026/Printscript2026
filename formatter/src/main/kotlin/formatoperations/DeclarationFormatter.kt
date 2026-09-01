package formatoperations

import ast.ASTNode
import ast.DeclarationNode
import formatoperations.commons.SpaceHandler
import formatter.Formatter

class DeclarationFormatter(
    private val allowedDeclarationKeywords: List<String>,
    private val allowedDataTypes: List<String>,
) : FormattingOperation {
    private val spaceHandler: SpaceHandler = SpaceHandler()

    override fun canHandle(astNode: ASTNode): Boolean = astNode is DeclarationNode

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
            formatOperationsList
                .find { it.canHandle(declarationNode.expr) }
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

        val equal = spaceHandler.handleSpace("=", spaceAroundEquals, spaceAroundEquals)
        val colon = spaceHandler.handleSpace(":", spaceBeforeColon, spaceAfterColon)

        return "$declKeywordValue $id$colon$dataType$equal$exprValue"
    }

    private fun allowedDeclarationKeyword(declKeyword: String): Boolean =
        allowedDeclarationKeywords.contains(
            declKeyword,
        )

    private fun allowedDataType(dataType: String): Boolean =
        allowedDataTypes.contains(
            dataType,
        )
}
