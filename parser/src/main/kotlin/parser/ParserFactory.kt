package parser

import factories.ASTFactory
import factories.AssignationFactory
import factories.ConditionalFactory
import factories.DeclarationFactory
import factories.FunctionFactory
import factories.PrintlnFactory
import version.Version

object ParserFactory {
    /** Las sentencias que la versión sabe construir. 1.1 es 1.0 más el `if`, que va primero. */
    fun factoriesFor(version: Version): List<ASTFactory> =
        when (version) {
            Version.V1_0 -> listOf(PrintlnFactory(), DeclarationFactory(), AssignationFactory(), FunctionFactory())
            Version.V1_1 -> listOf(ConditionalFactory { Parser.forVersion(Version.V1_1) }) + factoriesFor(Version.V1_0)
        }
}
