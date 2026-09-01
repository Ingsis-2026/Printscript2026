package parser

import factories.ASTFactory
import factories.AssignationFactory
import factories.ConditionalFactory
import factories.DeclarationFactory
import factories.FunctionFactory
import factories.PrintlnFactory

object ParserFactory {
    fun defaultFactories(): List<ASTFactory> =
        listOf(
            ConditionalFactory { forVersion("1.1") },
            PrintlnFactory(),
            DeclarationFactory(),
            AssignationFactory(),
            FunctionFactory(),
        )

    fun forVersion(version: String): Parser =
        when (version) {
            "1.0" ->
                Parser(
                    listOf(
                        PrintlnFactory(),
                        DeclarationFactory(),
                        AssignationFactory(),
                        FunctionFactory(),
                    ),
                )
            "1.1" ->
                Parser(
                    listOf(
                        ConditionalFactory { forVersion("1.1") },
                        PrintlnFactory(),
                        DeclarationFactory(),
                        AssignationFactory(),
                        FunctionFactory(),
                    ),
                )
            else -> throw IllegalArgumentException("Unsupported version: $version")
        }
}
