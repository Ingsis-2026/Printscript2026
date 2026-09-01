package interpreter.evaluators

import interpreter.InterpreterException

/**
 * Una operación aritmética expresada para cada tipo numérico soportado.
 */
internal class NumericOperation(
    val onInt: (Int, Int) -> Any,
    val onFloat: (Float, Float) -> Any,
    val onDouble: (Double, Double) -> Any,
)

/**
 * Promoción de operandos numéricos para las operaciones binarias.
 *
 * Se admiten los pares del mismo tipo (Int, Float, Double) y las mezclas de Int
 * con Float o Double. Cualquier otra combinación —por ejemplo Float con Double—
 * no está soportada y lanza [InterpreterException].
 */
internal object ArithmeticOperations {
    fun compute(
        leftValue: Any,
        rightValue: Any,
        operator: String,
        operation: NumericOperation,
    ): Any =
        sameType(leftValue, rightValue, operation)
            ?: mixedInt(leftValue, rightValue, operation)
            ?: throw InterpreterException("Unsupported operands for $operator")

    private fun sameType(
        leftValue: Any,
        rightValue: Any,
        operation: NumericOperation,
    ): Any? =
        when {
            leftValue is Int && rightValue is Int -> operation.onInt(leftValue, rightValue)
            leftValue is Float && rightValue is Float -> operation.onFloat(leftValue, rightValue)
            leftValue is Double && rightValue is Double -> operation.onDouble(leftValue, rightValue)
            else -> null
        }

    private fun mixedInt(
        leftValue: Any,
        rightValue: Any,
        operation: NumericOperation,
    ): Any? =
        when {
            leftValue is Int && rightValue is Float -> operation.onFloat(leftValue.toFloat(), rightValue)
            leftValue is Float && rightValue is Int -> operation.onFloat(leftValue, rightValue.toFloat())
            leftValue is Int && rightValue is Double -> operation.onDouble(leftValue.toDouble(), rightValue)
            leftValue is Double && rightValue is Int -> operation.onDouble(leftValue, rightValue.toDouble())
            else -> null
        }
}
