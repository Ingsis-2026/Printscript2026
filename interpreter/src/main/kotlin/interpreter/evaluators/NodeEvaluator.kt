package interpreter.evaluators

import ast.ASTNode
import interpreter.Interpreter

/**
 * Sabe ejecutar un tipo de nodo del AST.
 *
 * El contrato entre los dos métodos es lo que hace segura la implementación de [evaluate]:
 * el [Interpreter] recorre su lista y sólo llama a [evaluate] del primer evaluador cuyo
 * [canEvaluate] devolvió `true`. Por eso cada implementación puede empezar bajando el nodo a
 * su tipo (`node as XNode`) sin volver a verificarlo.
 *
 * El evaluador recibe el [Interpreter] entero, y no sólo el nodo, porque necesita dos cosas de
 * él: evaluar los hijos con [Interpreter.execute] —que es el único punto de despacho y donde
 * los errores reciben su posición— y llegar a las variables y al Printer/Reader inyectados.
 */
interface NodeEvaluator {
    /** Si este evaluador es el que sabe ejecutar [node]. */
    fun canEvaluate(node: ASTNode): Boolean

    /** Ejecuta [node]. Sólo se llama cuando [canEvaluate] dijo que sí. */
    fun evaluate(
        node: ASTNode,
        interpreter: Interpreter,
    ): Any?
}
