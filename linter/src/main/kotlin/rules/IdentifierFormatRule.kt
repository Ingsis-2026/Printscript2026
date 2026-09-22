package rules

import ast.ASTNode
import linter.BrokenRule

/**
 * Exige que todo identificador de la sentencia esté escrito en [format].
 *
 * Se revisa cada aparición y no sólo la declaración, así que un nombre mal escrito se informa
 * tantas veces como se lo use.
 */
class IdentifierFormatRule(
    private val format: IdentifierFormat,
) : Rule {
    override fun check(statement: ASTNode): List<BrokenRule> =
        statement
            .insideStatement()
            .flatMap { identifiersIn(it) }
            .filterNot { format.matches(it.name) }
            .map { BrokenRule(format.message, it.position) }
            .toList()
}
