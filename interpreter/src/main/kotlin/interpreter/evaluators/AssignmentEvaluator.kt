package interpreter.evaluators

import ast.ASTNode
import ast.AssignationNode
import ast.DataType
import interpreter.ExternalInput
import interpreter.Interpreter
import interpreter.InterpreterException

class AssignmentEvaluator : NodeEvaluator {
    override fun canEvaluate(node: ASTNode): Boolean = node is AssignationNode

    override fun evaluate(
        node: ASTNode,
        interpreter: Interpreter,
    ): Any? {
        val assignation = node as AssignationNode
        val value =
            interpreter.execute(assignation.expression)
                ?: throw InterpreterException("Invalid assignment in Assignment")

        val resolved = resolveExternalInput(value, assignation.id, interpreter)

        rejectInvalidReassignment(assignation.id, resolved, interpreter)

        interpreter.variables.assign(assignation.id, resolved)
        return resolved
    }

    /** El tipo de un valor leído de afuera lo fija el tipo declarado de la variable destino. */
    private fun resolveExternalInput(
        value: Any,
        id: String,
        interpreter: Interpreter,
    ): Any {
        if (value !is ExternalInput) return value
        val declaredType =
            interpreter.variables.declarationOf(id)?.declaredType
                ?: throw InterpreterException("No se puede asignar ${value.origin} a '$id': la variable no fue declarada")
        return value.asType(declaredType)
    }

    private fun rejectInvalidReassignment(
        id: String,
        value: Any,
        interpreter: Interpreter,
    ) {
        val declaration = interpreter.variables.declarationOf(id)
        if (declaration != null && declaration.isConstant) {
            throw InterpreterException("No es posible reasignar una variable de tipo ${declaration.keyword}")
        }

        val existing = interpreter.variables.valueOf(id) ?: return
        if (dataTypeOf(existing) != dataTypeOf(value)) {
            throw InterpreterException("Invalid expression for type ${dataTypeOf(existing).keyword}")
        }
    }

    /** Un Int y un Double son el mismo `number`: reasignar 5 con 2.5 es válido. */
    private fun dataTypeOf(value: Any): DataType =
        when (value) {
            is Number -> DataType.NUMBER
            is String -> DataType.STRING
            is Boolean -> DataType.BOOLEAN
            else -> throw InterpreterException("El valor $value no tiene un tipo de PrintScript")
        }
}
