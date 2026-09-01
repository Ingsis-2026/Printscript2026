package formatoperations

import ast.ASTNode
import ast.AssignationNode
import formatoperations.commons.SpaceHandler
import formatter.Formatter

class AssignationFormatter : FormattingOperation {
    private val spaceHandler = SpaceHandler()

    override fun canHandle(astNode: ASTNode): Boolean = astNode is AssignationNode

    override fun format(
        node: ASTNode,
        formatter: Formatter,
    ): String {
        if (!canHandle(node)) error("Node isn't a AssignationNode") else node as AssignationNode

        val spaceAroundEquals: Any? = formatter.getRules()["spaceAroundEquals"]
        if (spaceAroundEquals == null || spaceAroundEquals !is Boolean) error("spaceAroundEquals is not a boolean")

        val left = node.id
        val equals = spaceHandler.handleSpace("=", spaceAroundEquals, spaceAroundEquals)
        val right = formatter.format(node.expression)

        return "$left$equals$right"
    }
}
