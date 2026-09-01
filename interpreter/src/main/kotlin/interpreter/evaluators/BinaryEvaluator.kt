package interpreter.evaluators

import ast.ASTNode
import ast.BinaryNode
import interpreter.Interpreter
import interpreter.InterpreterException

class BinaryEvaluator : NodeEvaluator {
    override fun canEvaluate(node: ASTNode): Boolean = node is BinaryNode

    override fun evaluate(
        node: ASTNode,
        interpreter: Interpreter,
    ): Any? {
        val binary = node as BinaryNode
        val leftValue = interpreter.execute(binary.left) ?: throw InterpreterException("Invalid left operand")
        val rightValue = interpreter.execute(binary.right) ?: throw InterpreterException("Invalid right operand")
        val operator = binary.operator.value

        return when (operator) {
            "+" -> handleAddition(leftValue, rightValue)
            "-" -> ArithmeticOperations.compute(leftValue, rightValue, operator, SUBTRACTION)
            "*" -> handleMultiplication(leftValue, rightValue)
            "/" -> handleDivision(leftValue, rightValue)
            ">" -> handleComparison(leftValue, rightValue, operator) { a, b -> a > b }
            "<" -> handleComparison(leftValue, rightValue, operator) { a, b -> a < b }
            else -> throw InterpreterException("Unsupported operator: $operator")
        }
    }

    private fun handleAddition(
        leftValue: Any,
        rightValue: Any,
    ): Any =
        concatenate(leftValue, rightValue)
            ?: ArithmeticOperations.compute(leftValue, rightValue, "+", ADDITION)

    private fun handleMultiplication(
        leftValue: Any,
        rightValue: Any,
    ): Any {
        if (leftValue is String || rightValue is String) {
            throw InterpreterException("Invalid operation: cannot multiply a string by a number")
        }
        return ArithmeticOperations.compute(leftValue, rightValue, "*", MULTIPLICATION)
    }

    private fun handleDivision(
        leftValue: Any,
        rightValue: Any,
    ): Any {
        if (leftValue is String || rightValue is String) {
            throw InterpreterException("Invalid operation: cannot divide a string by a number")
        }
        val division =
            NumericOperation(
                onInt = { a, b ->
                    checkDivisorNotZero(b)
                    a / b
                },
                onFloat = { a, b ->
                    checkDivisorNotZero(b)
                    a / b
                },
                onDouble = { a, b ->
                    checkDivisorNotZero(b)
                    a / b
                },
            )
        return ArithmeticOperations.compute(leftValue, rightValue, "/", division)
    }

    private fun handleComparison(
        leftValue: Any,
        rightValue: Any,
        operator: String,
        compare: (Int, Int) -> Boolean,
    ): Boolean =
        if (leftValue is Int && rightValue is Int) {
            compare(leftValue, rightValue)
        } else {
            throw InterpreterException("Unsupported operands for $operator")
        }

    /**
     * Concatenación textual admitida por `+`: String+String, Int+String y String+Int.
     * Devuelve `null` cuando el par de operandos no es una concatenación, para que
     * [handleAddition] continúe con la aritmética numérica.
     */
    private fun concatenate(
        leftValue: Any,
        rightValue: Any,
    ): String? =
        when {
            leftValue is String && rightValue is String -> leftValue + rightValue
            leftValue is Int && rightValue is String -> leftValue.toString() + rightValue
            leftValue is String && rightValue is Int -> leftValue + rightValue.toString()
            else -> null
        }

    private fun checkDivisorNotZero(divisor: Number) {
        if (divisor.toDouble() == 0.0) throw InterpreterException("Division by zero")
    }

    private companion object {
        val ADDITION = NumericOperation({ a, b -> a + b }, { a, b -> a + b }, { a, b -> a + b })
        val SUBTRACTION = NumericOperation({ a, b -> a - b }, { a, b -> a - b }, { a, b -> a - b })
        val MULTIPLICATION = NumericOperation({ a, b -> a * b }, { a, b -> a * b }, { a, b -> a * b })
    }
}
