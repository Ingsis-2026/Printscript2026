package parser

import ast.AssignationNode
import ast.BinaryNode
import ast.DeclarationNode
import ast.LiteralNode
import ast.PrintNode
import factories.ConditionalFactory
import lexer.Lexer
import lexer.TokenMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import token.Token
import token.TokenPosition
import token.TokenType

class ParserTests {
    @Test
    fun `test parser execution with assignation`() {
        val tokens =
            listOf(
                Token(TokenType.IDENTIFIER, "var", TokenPosition(0, 4), TokenPosition(1, 9)),
                Token(TokenType.ASSIGNATION, "=", TokenPosition(0, 19), TokenPosition(1, 20)),
                Token(TokenType.LITERAL, "hello", TokenPosition(0, 21), TokenPosition(1, 27)),
                Token(TokenType.PUNCTUATOR, ";", TokenPosition(0, 28), TokenPosition(1, 29)),
            )

        val parser = Parser()
        val asts = parser.execute(tokens)

        val assignationNode = asts[0] as AssignationNode

        assertEquals("var", assignationNode.id)
        assertEquals("hello", (assignationNode.expression as LiteralNode).value)
    }

    @Test
    fun `test parser execution with declaration`() {
        val tokens =
            listOf(
                Token(TokenType.KEYWORD, "let", TokenPosition(0, 0), TokenPosition(1, 3)),
                Token(TokenType.IDENTIFIER, "x", TokenPosition(0, 4), TokenPosition(1, 5)),
                Token(TokenType.DECLARATOR, ":", TokenPosition(0, 6), TokenPosition(1, 7)),
                Token(TokenType.DATA_TYPE, "number", TokenPosition(0, 8), TokenPosition(1, 14)),
                Token(TokenType.PUNCTUATOR, ";", TokenPosition(0, 15), TokenPosition(1, 16)),
            )

        val parser = Parser()
        val asts = parser.execute(tokens)

        assertEquals(1, asts.size)

        val ast = asts[0] as DeclarationNode

        assertEquals(TokenType.KEYWORD, ast.declType)
        assertEquals("x", ast.id)
        assertEquals(TokenType.DATA_TYPE, ast.dataType)
        assertEquals("number", ast.dataTypeValue)
    }

    @Test
    fun `test parser execution with multiple declarations in one line`() {
        val tokens =
            listOf(
                Token(TokenType.KEYWORD, "let", TokenPosition(0, 0), TokenPosition(1, 3)),
                Token(TokenType.IDENTIFIER, "x", TokenPosition(0, 4), TokenPosition(1, 5)),
                Token(TokenType.DECLARATOR, ":", TokenPosition(0, 6), TokenPosition(1, 7)),
                Token(TokenType.DATA_TYPE, "number", TokenPosition(0, 8), TokenPosition(1, 14)),
                Token(TokenType.PUNCTUATOR, ";", TokenPosition(0, 15), TokenPosition(1, 16)),
                Token(TokenType.KEYWORD, "let", TokenPosition(1, 17), TokenPosition(2, 20)),
                Token(TokenType.IDENTIFIER, "y", TokenPosition(1, 21), TokenPosition(2, 22)),
                Token(TokenType.DECLARATOR, ":", TokenPosition(1, 23), TokenPosition(2, 24)),
                Token(TokenType.DATA_TYPE, "string", TokenPosition(1, 25), TokenPosition(2, 31)),
                Token(TokenType.PUNCTUATOR, ";", TokenPosition(1, 32), TokenPosition(2, 33)),
            )

        val parser = Parser()
        val asts = parser.execute(tokens)

        assertEquals(2, asts.size)

        val firstDeclaration = asts[0] as DeclarationNode
        assertEquals("x", firstDeclaration.id)
        assertEquals(TokenType.DATA_TYPE, firstDeclaration.dataType)

        val secondDeclaration = asts[1] as DeclarationNode
        assertEquals("y", secondDeclaration.id)
        assertEquals(TokenType.DATA_TYPE, secondDeclaration.dataType)
    }

    @Test
    fun `test parser with lexer input, assignation, declaration & println`() {
        val input =
            """
            let x:number = 42;
            let y:number = 10;
            println(x + y);
            """.trimIndent()

        val tokenMapper = TokenMapper("1.0")
        val lexer = Lexer(tokenMapper)

        val tokens = lexer.execute(input)
        val parser = Parser()
        val trees = parser.execute(tokens)

        assertEquals(3, trees.size)

        val firstTree = trees[0] as DeclarationNode
        assertEquals("x", firstTree.id)
        assertEquals(TokenType.KEYWORD, firstTree.declType)
        assertEquals(TokenType.DATA_TYPE, firstTree.dataType)

        val firstRightNode = firstTree.expr as LiteralNode
        assertEquals("42", firstRightNode.value)

        val secondTree = trees[1] as DeclarationNode
        assertEquals("y", secondTree.id)
        assertEquals(TokenType.KEYWORD, secondTree.declType)
        assertEquals(TokenType.DATA_TYPE, secondTree.dataType)

        val secondRightNode = secondTree.expr as LiteralNode
        assertEquals("10", secondRightNode.value)

        val thirdTree = trees[2] as PrintNode
        val expressionNode = thirdTree.expression
        if (expressionNode is BinaryNode) {
            assertEquals("+", expressionNode.operator.value)
            assertEquals("x", (expressionNode.left as LiteralNode).value)
            assertEquals("y", (expressionNode.right as LiteralNode).value)
        } else if (expressionNode is LiteralNode) {
            assertEquals("x", expressionNode.value)
        }
    }

    @Test
    fun `test parser with lexer input, data type inconsistent with the expression`() {
        val input1 =
            """
            let x:number = "hola"
            """.trimIndent()

        val input2 =
            """
            let x:string = 1
            """.trimIndent()

        val tokenMapper = TokenMapper("1.0")
        val lexer = Lexer(tokenMapper)

        val tokens1 = lexer.execute(input1)
        val tokens2 =
            lexer.execute(
                input2,
            )
        val parser = Parser()

        // Assert that a specific exception is thrown
        assertThrows<Exception> { parser.execute(tokens1) }
        assertThrows<Exception> { parser.execute(tokens2) }
    }

    private val parser = Parser()
    private val startPos = TokenPosition(0, 0)
    private val endPos = TokenPosition(0, 1)

    @Test
    fun `test incorrect statement without ending symbol throws exception`() {
        val tokens =
            listOf(
                Token(TokenType.CONDITIONAL, "if", startPos, endPos),
                Token(TokenType.IDENTIFIER, "x", startPos, endPos),
                Token(TokenType.ASSIGNATION, "=", startPos, endPos),
                Token(TokenType.NUMBERLITERAL, "10", startPos, endPos),
            )

        val exception = assertThrows<Exception> { parser.execute(tokens) }
        assertEquals("las sentencias deben finalizar con \";\", \"}\" o \"{\"", exception.message)
    }

    @Test
    fun `test missing opening brace in if block throws exception`() {
        val tokens =
            listOf(
                Token(TokenType.CONDITIONAL, "if", startPos, endPos),
                Token(TokenType.PUNCTUATOR, "(", startPos, endPos),
                Token(TokenType.LITERAL, "true", startPos, endPos),
                Token(TokenType.PUNCTUATOR, ")", startPos, endPos),
                // Missing opening brace {
                Token(TokenType.KEYWORD, "println", startPos, endPos),
                Token(TokenType.PUNCTUATOR, "(", startPos, endPos),
                Token(TokenType.LITERAL, "\"if block\"", startPos, endPos),
                Token(TokenType.PUNCTUATOR, ")", startPos, endPos),
                Token(TokenType.PUNCTUATOR, ";", startPos, endPos),
                Token(TokenType.PUNCTUATOR, "}", startPos, endPos),
            )

        val exception = assertThrows<RuntimeException> { ConditionalFactory().createAST(tokens) }
        assertEquals("Error parsing block: missing or unbalanced braces", exception.message)
    }

    @Test
    fun `test missing closing brace in if block throws exception`() {
        val tokens =
            listOf(
                Token(TokenType.CONDITIONAL, "if", startPos, endPos),
                Token(TokenType.PUNCTUATOR, "(", startPos, endPos),
                Token(TokenType.LITERAL, "true", startPos, endPos),
                Token(TokenType.PUNCTUATOR, ")", startPos, endPos),
                Token(TokenType.PUNCTUATOR, "{", startPos, endPos),
                Token(TokenType.KEYWORD, "println", startPos, endPos),
                Token(TokenType.PUNCTUATOR, "(", startPos, endPos),
                Token(TokenType.LITERAL, "\"if block\"", startPos, endPos),
                Token(TokenType.PUNCTUATOR, ")", startPos, endPos),
                Token(TokenType.PUNCTUATOR, ";", startPos, endPos),
                // Missing closing brace }
            )

        val exception = assertThrows<RuntimeException> { ConditionalFactory().createAST(tokens) }
        assertEquals("Error parsing block: missing or unbalanced braces", exception.message)
    }

    @Test
    fun `test execute with missing closing brace`() {
        val tokens =
            listOf(
                Token(TokenType.CONDITIONAL, "if", TokenPosition(0, 2), TokenPosition(1, 3)),
                Token(TokenType.PARENTHESIS, "(", TokenPosition(2, 3), TokenPosition(1, 4)),
                Token(TokenType.NUMBERLITERAL, "1", TokenPosition(3, 4), TokenPosition(1, 5)),
                Token(TokenType.PARENTHESIS, ")", TokenPosition(4, 5), TokenPosition(1, 6)),
                Token(TokenType.PUNCTUATOR, "{", TokenPosition(5, 6), TokenPosition(1, 7)),
                Token(TokenType.NUMBERLITERAL, "2", TokenPosition(6, 7), TokenPosition(1, 8)),
                // Missing closing brace
            )

        assertThrows<Exception> {
            parser.execute(tokens)
        }
    }

    @Test
    fun `test execute with missing parentheses`() {
        val tokens =
            listOf(
                Token(TokenType.CONDITIONAL, "if", TokenPosition(0, 2), TokenPosition(1, 3)),
                Token(TokenType.NUMBERLITERAL, "1", TokenPosition(0, 3), TokenPosition(1, 4)),
                Token(TokenType.PUNCTUATOR, "{", TokenPosition(0, 4), TokenPosition(1, 5)),
                Token(TokenType.NUMBERLITERAL, "2", TokenPosition(0, 5), TokenPosition(1, 6)),
                Token(TokenType.PUNCTUATOR, "}", TokenPosition(0, 6), TokenPosition(1, 7)),
                Token(TokenType.CONDITIONAL, "else", TokenPosition(0, 7), TokenPosition(1, 8)),
                Token(TokenType.PUNCTUATOR, "{", TokenPosition(0, 8), TokenPosition(1, 9)),
                Token(TokenType.NUMBERLITERAL, "3", TokenPosition(0, 9), TokenPosition(1, 10)),
                Token(TokenType.PUNCTUATOR, "}", TokenPosition(0, 10), TokenPosition(1, 11)),
            )

        assertThrows<Exception> {
            parser.execute(tokens)
        }
    }

    @Test
    fun `test parser version 1_0 does not support conditionals`() {
        val version10Parser = Parser.forVersion("1.0")
        val tokens =
            listOf(
                Token(TokenType.CONDITIONAL, "if", startPos, endPos),
                Token(TokenType.PARENTHESIS, "(", startPos, endPos),
                Token(TokenType.BOOLEANLITERAL, "true", startPos, endPos),
                Token(TokenType.PARENTHESIS, ")", startPos, endPos),
                Token(TokenType.PUNCTUATOR, "{", startPos, endPos),
                Token(TokenType.KEYWORD, "println", startPos, endPos),
                Token(TokenType.PARENTHESIS, "(", startPos, endPos),
                Token(TokenType.NUMBERLITERAL, "1", startPos, endPos),
                Token(TokenType.PARENTHESIS, ")", startPos, endPos),
                Token(TokenType.PUNCTUATOR, ";", startPos, endPos),
                Token(TokenType.PUNCTUATOR, "}", startPos, endPos),
            )

        assertThrows<Exception> {
            version10Parser.execute(tokens)
        }
    }

    @Test
    fun `test parser with custom injected factories`() {
        val customParser = Parser(listOf(factories.PrintlnFactory()))
        val printTokens =
            listOf(
                Token(TokenType.FUNCTION, "println", startPos, endPos),
                Token(TokenType.PARENTHESIS, "(", startPos, endPos),
                Token(TokenType.NUMBERLITERAL, "42", startPos, endPos),
                Token(TokenType.PARENTHESIS, ")", startPos, endPos),
                Token(TokenType.PUNCTUATOR, ";", startPos, endPos),
            )
        val result = customParser.execute(printTokens)
        assertEquals(1, result.size)
        assertEquals(true, result[0] is PrintNode)
    }
}
