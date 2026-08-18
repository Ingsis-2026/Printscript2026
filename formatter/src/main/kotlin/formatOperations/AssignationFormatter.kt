package formatOperations

import ast.ASTNode
import ast.AssignationNode
import formatOperations.commons.HandleSemicolon
import formatOperations.commons.HandleSpace
import formatter.Formatter

class AssignationFormatter : FormattingOperation {
    private val handleSpace = HandleSpace()
    private val handleSemicolon = HandleSemicolon()

    override fun canHandle(astNode: ASTNode): Boolean {
        return astNode is AssignationNode
    }

    override fun format(
        node: ASTNode,
        formatter: Formatter,
    ): String {
        if (!canHandle(node)) error("Node isn't a AssignationNode") else node as AssignationNode

        val spaceAroundEquals: Any? = formatter.getRules()["spaceAroundEquals"]
        if (spaceAroundEquals == null || spaceAroundEquals !is Boolean) error("spaceAroundEquals is not a boolean")

        val left = node.id
        val equals = handleSpace.handleSpace("=", spaceAroundEquals, spaceAroundEquals)
        val right = formatter.format(node.expression)

        return "$left$equals$right"
    }
}
