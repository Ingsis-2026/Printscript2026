package rules

import ast.ASTNode
import linter.BrokenRule

/**
 * Una convención que el fuente tiene que cumplir.
 *
 * Recibe una sentencia y devuelve las violaciones que encontró en ella. No lanza: encontrar un
 * problema no interrumpe el análisis, porque la consigna pide informarlos todos y no sólo el
 * primero.
 */
interface Rule {
    fun check(statement: ASTNode): List<BrokenRule>
}
