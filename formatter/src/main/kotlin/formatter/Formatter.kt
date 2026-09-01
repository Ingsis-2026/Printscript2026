package formatter

import ast.ASTNode

interface Formatter {
    fun format(input: String): String

    fun format(astNode: ASTNode): String

    /**
     * Formatea un fuente sentencia por sentencia, de forma perezosa.
     *
     * Está en la interfaz —y no sólo en la implementación— porque es el punto de entrada para
     * un fuente que no cabe en memoria: quien recibe un [Formatter] tiene que poder consumirlo
     * de a una sentencia sin conocer la clase concreta.
     */
    fun formatStatements(lines: Sequence<String>): Sequence<String>

    fun getRules(): Map<String, Any>
}
