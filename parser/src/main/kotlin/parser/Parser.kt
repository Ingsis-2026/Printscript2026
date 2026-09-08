package parser

import ast.ASTNode
import factories.ASTFactory
import token.Token

class Parser(
    private val factories: List<ASTFactory> = ParserFactory.defaultFactories(),
) {
    companion object {
        fun forVersion(version: String): Parser = ParserFactory.forVersion(version)
    }

    /**
     * Convierte un flujo de tokens en un flujo de nodos del AST, de forma perezosa.
     *
     * Cada sentencia se parsea recién cuando el consumidor pide el nodo siguiente, así que
     * un fuente que no cabe en memoria puede recorrerse de punta a punta: en ningún momento
     * se retienen ni todos los tokens ni todos los nodos.
     */
    fun execute(tokens: Sequence<Token>): Sequence<ASTNode> = nodesOf(StatementSplitter().split(tokens))

    /** Variante que materializa el resultado, para los usos que ya tienen todo en memoria. */
    fun execute(tokens: List<Token>): List<ASTNode> = execute(tokens.asSequence()).toList()

    /**
     * Parsea el cuerpo de un bloque, que llega con el `}` que lo cierra.
     *
     * Se distingue de [execute] porque en el nivel superior un `}` sin bloque abierto es un
     * error, mientras que acá es el cierre del bloque que envuelve al cuerpo: termina la
     * última sentencia, que por eso puede omitir el `;`.
     */
    fun executeBlockBody(tokens: List<Token>): List<ASTNode> =
        nodesOf(StatementSplitter(insideBlock = true).split(tokens.asSequence())).toList()

    private fun nodesOf(statements: Sequence<List<Token>>): Sequence<ASTNode> = statements.map { createNode(it) }

    private fun createNode(statement: List<Token>): ASTNode {
        val astFactory =
            determineFactory(statement)
                ?: throw ParserException(
                    "Can't handle this sentence",
                    statement.firstOrNull()?.getPosition(),
                    statement.lastOrNull()?.getFinalPosition(),
                )
        return astFactory.createAST(statement)
    }

    private fun determineFactory(tokens: List<Token>): ASTFactory? = factories.find { it.canHandle(tokens) }
}
