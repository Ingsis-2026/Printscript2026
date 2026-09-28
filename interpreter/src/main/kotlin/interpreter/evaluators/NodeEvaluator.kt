package interpreter.evaluators

import ast.ASTNode
import interpreter.Interpreter
import kotlin.reflect.KClass
import kotlin.reflect.cast

/**
 * Sabe ejecutar los nodos de tipo [nodeType].
 *
 * Recibe el [Interpreter] entero, y no sólo el nodo, para evaluar los hijos con
 * [Interpreter.execute] —el único punto de despacho, donde los errores reciben su posición— y
 * para llegar a las variables y al Printer/Reader inyectados.
 */
interface NodeEvaluator<T : ASTNode> {
    val nodeType: KClass<T>

    /** Ejecuta [node] y devuelve el valor que produce, o `null` si no produce ninguno —como una sentencia—. */
    fun evaluate(
        node: T,
        interpreter: Interpreter,
    ): Any?

    /** Para el [Interpreter], que elige este evaluador por el tipo del nodo y le pasa el nodo tal cual. */
    fun evaluateAny(
        node: ASTNode,
        interpreter: Interpreter,
    ): Any? = evaluate(nodeType.cast(node), interpreter)
}
