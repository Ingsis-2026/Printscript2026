package ast

import org.junit.jupiter.api.Test
import token.Token
import token.TokenPosition
import token.TokenType
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ASTNodeTests {
    @Test
    fun `test 001 literal node`() {
        val tokenPosition = TokenPosition(1, 1)
        val literalNode = LiteralNode("5", TokenType.NUMBERLITERAL, tokenPosition)

        assertEquals("5", literalNode.value)
        assertEquals(TokenType.NUMBERLITERAL, literalNode.type)
        assertEquals(tokenPosition, literalNode.position)
    }

    @Test
    fun `test 002 binary node`() {
        val left = LiteralNode("10", TokenType.NUMBERLITERAL, TokenPosition(1, 1))
        val right = LiteralNode("20", TokenType.NUMBERLITERAL, TokenPosition(1, 2))
        val operator = Token(TokenType.OPERATOR, "+", TokenPosition(1, 3), TokenPosition(1, 3))
        val binaryNode = BinaryNode(left, right, operator, TokenPosition(1, 3))

        assertEquals(left, binaryNode.left)
        assertEquals(right, binaryNode.right)
        assertEquals(operator, binaryNode.operator)
        assertEquals(TokenPosition(1, 3), binaryNode.position)
    }

    @Test
    fun `test 003 print node`() {
        val expr = LiteralNode("Hello, World!", TokenType.STRINGLITERAL, TokenPosition(1, 1))
        val printNode = PrintNode(expr, TokenPosition(1, 5))

        assertEquals(expr, printNode.expression)
        assertEquals(TokenPosition(1, 5), printNode.position)
    }

    @Test
    fun `test 004 declaration node`() {
        val expr = LiteralNode("10", TokenType.NUMBERLITERAL, TokenPosition(1, 5))
        val declNode =
            DeclarationNode(
                TokenType.DECLARATOR,
                "let",
                "x",
                TokenType.DATA_TYPE,
                "number",
                expr,
                TokenPosition(1, 1),
            )

        assertEquals("let", declNode.declValue)
        assertEquals("x", declNode.id)
        assertEquals(TokenType.DATA_TYPE, declNode.dataType)
        assertEquals("number", declNode.dataTypeValue)
        assertEquals(expr, declNode.expr)
        assertEquals(TokenPosition(1, 1), declNode.position)
    }

    @Test
    fun `test 005 assignation node`() {
        val expr = LiteralNode("15", TokenType.NUMBERLITERAL, TokenPosition(1, 5))
        val assignNode =
            AssignationNode(
                "y",
                expr,
                TokenType.ASSIGNATION,
                TokenPosition(1, 2),
            )

        assertEquals("y", assignNode.id)
        assertEquals(expr, assignNode.expression)
        assertEquals(TokenType.ASSIGNATION, assignNode.valType)
        assertEquals(TokenPosition(1, 2), assignNode.position)
    }

    @Test
    fun `test 006 block node`() {
        val expr1 = LiteralNode("x", TokenType.IDENTIFIER, TokenPosition(1, 1))
        val expr2 = LiteralNode("y", TokenType.IDENTIFIER, TokenPosition(1, 2))
        val blockNode = BlockNode(listOf(expr1, expr2), TokenPosition(1, 3))

        assertEquals(2, blockNode.nodes.size)
        assertEquals(expr1, blockNode.nodes[0])
        assertEquals(expr2, blockNode.nodes[1])
        assertEquals(TokenPosition(1, 3), blockNode.position)
    }

    @Test
    fun `test 007 conditional node with else block`() {
        val condition = LiteralNode("true", TokenType.BOOLEANLITERAL, TokenPosition(1, 1))
        val thenBlock = LiteralNode("1", TokenType.NUMBERLITERAL, TokenPosition(1, 2))
        val elseBlock = LiteralNode("0", TokenType.NUMBERLITERAL, TokenPosition(1, 3))
        val conditionalNode = ConditionalNode(condition, thenBlock, elseBlock, TokenPosition(1, 4))

        assertEquals(condition, conditionalNode.condition)
        assertEquals(thenBlock, conditionalNode.thenBlock)
        assertEquals(elseBlock, conditionalNode.elseBlock)
        assertEquals(TokenPosition(1, 4), conditionalNode.position)
    }

    @Test
    fun `test 008 conditional node without else block`() {
        val condition = LiteralNode("false", TokenType.BOOLEANLITERAL, TokenPosition(1, 1))
        val thenBlock = LiteralNode("0", TokenType.NUMBERLITERAL, TokenPosition(1, 2))
        val conditionalNode = ConditionalNode(condition, thenBlock, null, TokenPosition(1, 4))

        assertEquals(condition, conditionalNode.condition)
        assertEquals(thenBlock, conditionalNode.thenBlock)
        assertNull(conditionalNode.elseBlock)
        assertEquals(TokenPosition(1, 4), conditionalNode.position)
    }

    @Test
    fun `test 009 function node`() {
        val expr = LiteralNode("5", TokenType.NUMBERLITERAL, TokenPosition(1, 5))
        val functionNode =
            FunctionNode(
                TokenType.FUNCTION,
                "myFunction",
                expr,
                TokenPosition(1, 1),
            )

        assertEquals(TokenType.FUNCTION, functionNode.type)
        assertEquals("myFunction", functionNode.functionName)
        assertEquals(expr, functionNode.expression)
        assertEquals(TokenPosition(1, 1), functionNode.position)
    }

    @Test
    fun `test 010 nil node`() {
        val nilNode = NilNode
        assertEquals(TokenPosition(0, 0), nilNode.position)
    }
}
