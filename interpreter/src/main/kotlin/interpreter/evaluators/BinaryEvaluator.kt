package interpreter.evaluators

import ast.ASTNode
import ast.BinaryNode
import ast.DataType
import interpreter.ExternalInput
import interpreter.Interpreter
import interpreter.InterpreterException

class BinaryEvaluator : NodeEvaluator<BinaryNode> {
    override val nodeType = BinaryNode::class

    override fun evaluate(
        node: BinaryNode,
        interpreter: Interpreter,
    ): Any? {
        val leftValue = operandValue(node.left, interpreter) ?: throw InterpreterException("Invalid left operand")
        val rightValue = operandValue(node.right, interpreter) ?: throw InterpreterException("Invalid right operand")

        return when (val operator = node.operator.value) {
            "+" -> add(leftValue, rightValue)
            "-" -> subtract(leftValue, rightValue)
            "*" -> multiply(leftValue, rightValue)
            "/" -> divide(leftValue, rightValue)
            ">" -> isGreaterThan(leftValue, rightValue)
            "<" -> isLessThan(leftValue, rightValue)
            else -> throw InterpreterException("Unsupported operator: $operator")
        }
    }

    /** Un valor leído de afuera que entra en una operación es un texto, igual que dentro de un println. */
    private fun operandValue(
        operand: ASTNode,
        interpreter: Interpreter,
    ): Any? {
        val value = interpreter.execute(operand)
        return if (value is ExternalInput) value.asType(DataType.STRING) else value
    }

    /** Con un string de por medio `+` concatena; si no, suma. */
    private fun add(
        leftValue: Any,
        rightValue: Any,
    ): Any =
        concatenate(leftValue, rightValue)
            ?: ArithmeticOperations.compute(leftValue, rightValue, "+", ADDITION)

    private fun subtract(
        leftValue: Any,
        rightValue: Any,
    ): Any = ArithmeticOperations.compute(leftValue, rightValue, "-", SUBTRACTION)

    private fun multiply(
        leftValue: Any,
        rightValue: Any,
    ): Any {
        if (leftValue is String || rightValue is String) {
            throw InterpreterException("Invalid operation: cannot multiply a string by a number")
        }
        return ArithmeticOperations.compute(leftValue, rightValue, "*", MULTIPLICATION)
    }

    private fun divide(
        leftValue: Any,
        rightValue: Any,
    ): Any {
        if (leftValue is String || rightValue is String) {
            throw InterpreterException("Invalid operation: cannot divide a string by a number")
        }
        val division =
            NumericOperation(
                onInt = { a, b ->
                    rejectZeroDivisor(b)
                    a / b
                },
                onDouble = { a, b ->
                    rejectZeroDivisor(b)
                    a / b
                },
            )
        return ArithmeticOperations.compute(leftValue, rightValue, "/", division)
    }

    private fun isGreaterThan(
        leftValue: Any,
        rightValue: Any,
    ): Boolean = compare(leftValue, rightValue, ">") { left, right -> left > right }

    private fun isLessThan(
        leftValue: Any,
        rightValue: Any,
    ): Boolean = compare(leftValue, rightValue, "<") { left, right -> left < right }

    /** Comparar sólo está definido entre enteros. */
    private fun compare(
        leftValue: Any,
        rightValue: Any,
        operator: String,
        comparison: (Int, Int) -> Boolean,
    ): Boolean =
        if (leftValue is Int && rightValue is Int) {
            comparison(leftValue, rightValue)
        } else {
            throw InterpreterException("Unsupported operands for $operator")
        }

    /**
     * Concatenación textual admitida por `+`: si la expresión incluye un "string" y un
     * "number" el resultado es "string". Cubre todo Number (enteros y decimales), no sólo Int.
     *
     * Devuelve `null` cuando el par de operandos no es una concatenación, para que
     * [add] continúe con la aritmética numérica.
     */
    private fun concatenate(
        leftValue: Any,
        rightValue: Any,
    ): String? =
        when {
            leftValue is String && rightValue is String -> leftValue + rightValue
            leftValue is Number && rightValue is String -> leftValue.toString() + rightValue
            leftValue is String && rightValue is Number -> leftValue + rightValue.toString()
            else -> null
        }

    private fun rejectZeroDivisor(divisor: Number) {
        if (divisor.toDouble() == 0.0) throw InterpreterException("Division by zero")
    }

    private companion object {
        val ADDITION = NumericOperation({ a, b -> a + b }, { a, b -> a + b })
        val SUBTRACTION = NumericOperation({ a, b -> a - b }, { a, b -> a - b })
        val MULTIPLICATION = NumericOperation({ a, b -> a * b }, { a, b -> a * b })
    }
}
