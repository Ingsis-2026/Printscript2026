package interpreter.evaluators

import interpreter.InterpreterException

/**
 * Una operación aritmética expresada para los dos tipos numéricos que puede haber en ejecución.
 */
internal class NumericOperation(
    val onInt: (Int, Int) -> Any,
    val onDouble: (Double, Double) -> Any,
)

/**
 * Promoción de los operandos de una operación binaria.
 *
 * PrintScript tiene un solo tipo `number`, que en ejecución es un Int o un Double: los dos únicos
 * lugares que producen un número —LiteralEvaluator y ExternalInput.asType— resuelven
 * `toIntOrNull() ?: toDoubleOrNull()`, así que no hay otro tipo numérico posible.
 *
 * La regla es una sola y vale para `+`, `-`, `*` y `/`: la cuenta es entera mientras los dos
 * operandos lo sean, y decimal apenas uno no lo sea. Un par que no sea numérico no se promueve
 * y lo rechaza quien lo pidió.
 */
internal object ArithmeticOperations {
    fun compute(
        leftValue: Any,
        rightValue: Any,
        operator: String,
        operation: NumericOperation,
    ): Any =
        when {
            leftValue is Int && rightValue is Int -> operation.onInt(leftValue, rightValue)
            leftValue is Number && rightValue is Number -> operation.onDouble(leftValue.toDouble(), rightValue.toDouble())
            else -> throw InterpreterException("Unsupported operands for $operator")
        }
}
