import ast.BinaryNode
import ast.ConditionalNode
import ast.DeclarationNode
import ast.FunctionNode
import ast.LiteralNode
import ast.PrintNode
import ast.Tokenizer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import token.Token
import token.TokenPosition
import token.TokenType

class TokenizerTests {
    @Test
    fun `test parseToTokens with LiteralNode`() {
        val literalNode = LiteralNode("42", TokenType.LITERAL, TokenPosition(1, 1))
        val tokenizer = Tokenizer()

        val tokens = tokenizer.parseToTokens(listOf(literalNode))

        val expectedTokens =
            listOf(
                Token(TokenType.LITERAL, "42", TokenPosition(1, 1), TokenPosition(1, 1)),
            )
        assertEquals(expectedTokens, tokens.flatten())
    }

    @Test
    fun `test parseToTokens with PrintNode`() {
        val expressionNode = LiteralNode("Hello, World!", TokenType.LITERAL, TokenPosition(1, 5))
        val printNode = PrintNode(expressionNode, TokenPosition(1, 1))
        val tokenizer = Tokenizer()

        val tokens = tokenizer.parseToTokens(listOf(printNode))

        val expectedTokens =
            listOf(
                Token(TokenType.FUNCTION, "print", TokenPosition(1, 1), TokenPosition(1, 1)),
                Token(TokenType.LITERAL, "Hello, World!", TokenPosition(1, 5), TokenPosition(1, 5)),
            )
        assertEquals(expectedTokens, tokens.flatten())
    }

    @Test
    fun `test parseToTokens with DeclarationNode`() {
        val declarationNode =
            DeclarationNode(
                TokenType.DECLARATOR,
                "let",
                "x",
                TokenType.DATA_TYPE,
                "number",
                LiteralNode("10", TokenType.LITERAL, TokenPosition(1, 5)),
                TokenPosition(1, 1),
            )
        val tokenizer = Tokenizer()

        val tokens = tokenizer.parseToTokens(listOf(declarationNode))

        val expectedTokens =
            listOf(
                Token(TokenType.IDENTIFIER, "x", TokenPosition(1, 1), TokenPosition(1, 1)),
                Token(TokenType.LITERAL, "10", TokenPosition(1, 5), TokenPosition(1, 5)),
            )
        assertEquals(expectedTokens, tokens.flatten())
    }

    @Test
    fun `test parseToTokens with ConditionalNode`() {
        val conditionNode = LiteralNode("true", TokenType.BOOLEAN, TokenPosition(1, 1))
        val thenNode = LiteralNode("Do something", TokenType.LITERAL, TokenPosition(1, 5))
        val conditionalNode = ConditionalNode(conditionNode, thenNode, null, TokenPosition(1, 1))
        val tokenizer = Tokenizer()

        val tokens = tokenizer.parseToTokens(listOf(conditionalNode))

        val expectedTokens =
            listOf(
                Token(TokenType.CONDITIONAL, "if", TokenPosition(1, 1), TokenPosition(1, 1)),
                Token(TokenType.BOOLEAN, "true", TokenPosition(1, 1), TokenPosition(1, 1)),
                Token(TokenType.LITERAL, "Do something", TokenPosition(1, 5), TokenPosition(1, 5)),
            )
        assertEquals(expectedTokens, tokens.flatten())
    }

    @Test
    fun `test parseToTokens with BinaryNode`() {
        // Crear nodos literales para el lado izquierdo y derecho del nodo binario
        val leftNode = LiteralNode("1", TokenType.LITERAL, TokenPosition(1, 1))
        val rightNode = LiteralNode("2", TokenType.LITERAL, TokenPosition(1, 3))

        // Crear un nodo binario que sume los dos nodos literales
        val binaryNode =
            BinaryNode(
                left = leftNode,
                right = rightNode,
                operator = Token(TokenType.OPERATOR, "+", TokenPosition(1, 2), TokenPosition(1, 2)),
                position = TokenPosition(1, 2),
            )

        // Instanciar el tokenizer y parsear los nodos
        val tokenizer = Tokenizer()
        val tokens = tokenizer.parseToTokens(listOf(binaryNode))

        // Definir los tokens esperados
        val expectedTokens =
            listOf(
                Token(TokenType.LITERAL, "1", TokenPosition(1, 1), TokenPosition(1, 1)),
                Token(TokenType.OPERATOR, "+", TokenPosition(1, 2), TokenPosition(1, 2)),
                Token(TokenType.LITERAL, "2", TokenPosition(1, 3), TokenPosition(1, 3)),
            )

        // Comparar los tokens generados con los esperados
        assertEquals(expectedTokens, tokens.flatten())
    }

    @Test
    fun `test parseToTokens with FunctionNode`() {
        val expressionNode = LiteralNode("42", TokenType.LITERAL, TokenPosition(1, 5))
        val functionNode = FunctionNode(TokenType.FUNCTION, "myfunction", expressionNode, TokenPosition(1, 1))
        val tokenizer = Tokenizer()
        val tokens = tokenizer.parseToTokens(listOf(functionNode))

        val expectedTokens =
            listOf(
                Token(TokenType.FUNCTION, "myfunction", TokenPosition(1, 1), TokenPosition(1, 1)),
                Token(TokenType.LITERAL, "42", TokenPosition(1, 5), TokenPosition(1, 5)),
            )
        assertEquals(expectedTokens, tokens.flatten())
    }

    // Agrega más pruebas según sea necesario para otros tipos de nodos.
}
