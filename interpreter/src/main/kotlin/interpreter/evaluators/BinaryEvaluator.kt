package interpreter.evaluators

import ast.ASTNode
import ast.BinaryNode
import interpreter.Interpreter

class BinaryEvaluator : NodeEvaluator {
    override fun canEvaluate(node: ASTNode): Boolean = node is BinaryNode

    override fun evaluate(
        node: ASTNode,
        interpreter: Interpreter,
    ): Any? {
        val binary = node as BinaryNode
        val leftValue = interpreter.execute(binary.left) ?: throw RuntimeException("Invalid left operand")
        val rightValue = interpreter.execute(binary.right) ?: throw RuntimeException("Invalid right operand")
        val operator = binary.operator.value

        return when (operator) {
            "+" -> handleAddition(leftValue, rightValue)
            "-" -> handleSubtraction(leftValue, rightValue)
            "*" -> handleMultiplication(leftValue, rightValue)
            "/" -> handleDivision(leftValue, rightValue)
            ">" -> handleGreaterThan(leftValue, rightValue)
            "<" -> handleLessThan(leftValue, rightValue)
            else -> throw RuntimeException("Unsupported operator: $operator")
        }
    }

    private fun handleAddition(
        leftValue: Any,
        rightValue: Any,
    ): Any? =
        when {
            leftValue is Int && rightValue is Int -> leftValue + rightValue
            leftValue is String && rightValue is String -> leftValue + rightValue
            leftValue is Int && rightValue is String -> leftValue.toString() + rightValue
            leftValue is String && rightValue is Int -> leftValue + rightValue.toString()
            leftValue is Float && rightValue is Float -> leftValue + rightValue
            leftValue is Double && rightValue is Double -> leftValue + rightValue
            leftValue is Int && rightValue is Float -> leftValue.toFloat() + rightValue
            leftValue is Float && rightValue is Int -> leftValue + rightValue.toFloat()
            leftValue is Int && rightValue is Double -> leftValue.toDouble() + rightValue
            leftValue is Double && rightValue is Int -> leftValue + rightValue.toDouble()
            else -> throw RuntimeException("Unsupported operands for +")
        }

    private fun handleSubtraction(
        leftValue: Any,
        rightValue: Any,
    ): Any? =
        when {
            leftValue is Int && rightValue is Int -> leftValue - rightValue
            leftValue is Float && rightValue is Float -> leftValue - rightValue
            leftValue is Double && rightValue is Double -> leftValue - rightValue
            leftValue is Int && rightValue is Float -> leftValue.toFloat() - rightValue
            leftValue is Float && rightValue is Int -> leftValue - rightValue.toFloat()
            leftValue is Int && rightValue is Double -> leftValue.toDouble() - rightValue
            leftValue is Double && rightValue is Int -> leftValue - rightValue.toDouble()
            else -> throw RuntimeException("Unsupported operands for -")
        }

    private fun handleMultiplication(
        leftValue: Any,
        rightValue: Any,
    ): Any? {
        if (leftValue is String || rightValue is String) {
            throw RuntimeException("Invalid operation: cannot multiply a string by a number")
        }
        return when {
            leftValue is Int && rightValue is Int -> leftValue * rightValue
            leftValue is Float && rightValue is Float -> leftValue * rightValue
            leftValue is Double && rightValue is Double -> leftValue * rightValue
            leftValue is Int && rightValue is Float -> leftValue.toFloat() * rightValue
            leftValue is Float && rightValue is Int -> leftValue * rightValue.toFloat()
            leftValue is Int && rightValue is Double -> leftValue.toDouble() * rightValue
            leftValue is Double && rightValue is Int -> leftValue * rightValue.toDouble()
            else -> throw RuntimeException("Unsupported operands for *")
        }
    }

    private fun handleDivision(
        leftValue: Any,
        rightValue: Any,
    ): Any? {
        if (leftValue is String || rightValue is String) {
            throw RuntimeException("Invalid operation: cannot divide a string by a number")
        }
        return when {
            leftValue is Int && rightValue is Int -> {
                if (rightValue == 0) throw RuntimeException("Division by zero")
                leftValue / rightValue
            }
            leftValue is Float && rightValue is Float -> {
                if (rightValue == 0f) throw RuntimeException("Division by zero")
                leftValue / rightValue
            }
            leftValue is Double && rightValue is Double -> {
                if (rightValue == 0.0) throw RuntimeException("Division by zero")
                leftValue / rightValue
            }
            leftValue is Int && rightValue is Float -> {
                if (rightValue == 0f) throw RuntimeException("Division by zero")
                leftValue.toFloat() / rightValue
            }
            leftValue is Float && rightValue is Int -> {
                if (rightValue == 0) throw RuntimeException("Division by zero")
                leftValue / rightValue.toFloat()
            }
            leftValue is Int && rightValue is Double -> {
                if (rightValue == 0.0) throw RuntimeException("Division by zero")
                leftValue.toDouble() / rightValue
            }
            leftValue is Double && rightValue is Int -> {
                if (rightValue == 0) throw RuntimeException("Division by zero")
                leftValue / rightValue.toDouble()
            }
            else -> throw RuntimeException("Unsupported operands for /")
        }
    }

    private fun handleGreaterThan(
        leftValue: Any,
        rightValue: Any,
    ): Any? =
        if (leftValue is Int && rightValue is Int) {
            leftValue > rightValue
        } else {
            throw RuntimeException("Unsupported operands for >")
        }

    private fun handleLessThan(
        leftValue: Any,
        rightValue: Any,
    ): Any? =
        if (leftValue is Int && rightValue is Int) {
            leftValue < rightValue
        } else {
            throw RuntimeException("Unsupported operands for <")
        }
}
