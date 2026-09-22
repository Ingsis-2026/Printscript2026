package rules

import ast.ASTNode
import ast.AssignationNode
import ast.BinaryNode
import ast.BlockNode
import ast.ConditionalNode
import ast.DeclarationNode
import ast.FunctionNode
import ast.LiteralNode
import ast.NilNode
import ast.PrintNode
import token.TokenPosition
import token.TokenType

/**
 * Las sentencias que hay en [node], empezando por sí mismo.
 *
 * Una sentencia dentro de un bloque es una sentencia: por eso un `println` adentro de un `if` se
 * revisa igual que uno de primer nivel, y su violación se informa en su propia posición y no en
 * la del `if` que lo contiene.
 */
internal fun statementsOf(node: ASTNode): Sequence<ASTNode> = sequenceOf(node) + bodyOf(node).flatMap { statementsOf(it) }

/**
 * Los nodos de la sentencia misma, sin entrar en sus bloques.
 *
 * Es lo que una regla mira para decidir sobre esta sentencia. Los bloques quedan afuera porque
 * sus sentencias se revisan por separado: mirarlos acá informaría cada violación dos veces.
 */
internal fun ASTNode.insideStatement(): Sequence<ASTNode> = sequenceOf(this) + childrenOf(this).flatMap { it.insideStatement() }

/** Un identificador escrito en el fuente, con el lugar donde se lo escribió. */
internal class Identifier(
    val name: String,
    val position: TokenPosition,
)

/**
 * Los identificadores que aparecen en [node]: el nombre que declara o asigna, y el nombre que
 * usa. Cada uso se informa por separado, no sólo la declaración.
 */
internal fun identifiersIn(node: ASTNode): Sequence<Identifier> =
    when (node) {
        is DeclarationNode -> sequenceOf(Identifier(node.id, node.position))
        is AssignationNode -> sequenceOf(Identifier(node.id, node.position))
        is LiteralNode ->
            if (node.type == TokenType.IDENTIFIER) sequenceOf(Identifier(node.value, node.position)) else emptySequence()
        else -> emptySequence()
    }

/** Una llamada con su argumento. `println` tiene nodo propio en el AST; el resto, no. */
internal class Call(
    val name: String,
    val argument: ASTNode,
)

internal fun callOf(node: ASTNode): Call? =
    when (node) {
        is PrintNode -> Call("println", node.expression)
        is FunctionNode -> Call(node.functionName, node.expression)
        else -> null
    }

/**
 * Las sentencias que [node] contiene en sus bloques.
 *
 * El `if` es la única sentencia con cuerpo, y [statementsIn] es el único lugar que abre un
 * bloque, así que un [BlockNode] nunca llega hasta acá.
 *
 * El `when` es exhaustivo a propósito: [ASTNode] es una clase sellada, así que agregar un tipo
 * de nodo deja de compilar acá hasta que se decida si tiene cuerpo.
 */
private fun bodyOf(node: ASTNode): Sequence<ASTNode> =
    when (node) {
        is ConditionalNode -> statementsIn(node.thenBlock) + statementsIn(node.elseBlock)
        is BlockNode, is LiteralNode, is BinaryNode, is PrintNode, is DeclarationNode,
        is AssignationNode, is FunctionNode, is NilNode,
        -> emptySequence()
    }

/** Un `if` sin `else` lo lleva en [NilNode], que no aporta sentencias. */
private fun statementsIn(block: ASTNode): Sequence<ASTNode> = if (block is BlockNode) block.nodes.asSequence() else emptySequence()

private fun childrenOf(node: ASTNode): Sequence<ASTNode> =
    when (node) {
        is BinaryNode -> sequenceOf(node.left, node.right)
        is PrintNode -> sequenceOf(node.expression)
        is DeclarationNode -> sequenceOf(node.expr)
        is AssignationNode -> sequenceOf(node.expression)
        is FunctionNode -> sequenceOf(node.expression)
        is ConditionalNode -> sequenceOf(node.condition)
        is BlockNode, is LiteralNode, is NilNode -> emptySequence()
    }
