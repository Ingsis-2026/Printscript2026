package parser

import ast.BlockNode
import ast.ConditionalNode
import ast.DeclarationNode
import ast.NilNode
import ast.PrintNode
import factories.ConditionalFactory
import lexer.Lexer
import lexer.TokenMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import token.Token
import token.TokenPosition
import token.TokenType

/**
 * El cuerpo de un bloque se sub-parsea aparte, así que hay que cubrirlo con el mismo detalle
 * que el nivel superior: varias sentencias, bloques anidados y a quién pertenece cada `else`.
 */
class BlockParsingTest {
    private val lexer = Lexer(TokenMapper("1.1"))
    private val parser = Parser.forVersion("1.1")

    private fun parse(source: String) = parser.execute(lexer.execute(source))

    private fun blockOf(node: Any?): BlockNode = (node as ConditionalNode).thenBlock as BlockNode

    private fun token(
        type: TokenType,
        value: String,
    ) = Token(type, value, TokenPosition(0, 0), TokenPosition(0, 0))

    @Test
    fun `a block keeps every statement, not just the first`() {
        val nodes = parse("if (a) { println(\"uno\"); println(\"dos\"); }")

        assertEquals(1, nodes.size)
        val body = blockOf(nodes[0]).nodes
        assertEquals(2, body.size)
        assertTrue(body.all { it is PrintNode })
    }

    @Test
    fun `a declaration inside a block is parsed as a declaration`() {
        val nodes = parse("if (a) { let b : boolean = false; println(b); }")

        val body = blockOf(nodes[0]).nodes
        assertEquals(2, body.size)
        assertTrue(body[0] is DeclarationNode)
        assertTrue(body[1] is PrintNode)
    }

    @Test
    fun `a nested conditional does not swallow the rest of the block`() {
        val nodes = parse("if (a) { if (b) { println(\"dentro\"); } println(\"fuera\"); }")

        val body = blockOf(nodes[0]).nodes
        assertEquals(2, body.size)
        assertTrue(body[0] is ConditionalNode)
        assertTrue(body[1] is PrintNode)
    }

    @Test
    fun `an else belongs to its own if and not to the enclosing one`() {
        val nodes = parse("if (a) { if (b) { println(\"si\"); } else { println(\"no\"); } }")

        val outer = nodes[0] as ConditionalNode
        assertEquals(NilNode, outer.elseBlock, "El if externo no tiene else")

        val inner = (outer.thenBlock as BlockNode).nodes[0] as ConditionalNode
        assertTrue(inner.elseBlock is BlockNode, "El else es del if interno")
    }

    @Test
    fun `the closing brace terminates the last statement of a block`() {
        val nodes = parse("if (a) { println(\"ok\") }")

        assertEquals(1, blockOf(nodes[0]).nodes.size)
    }

    @Test
    fun `an empty block yields an empty body`() {
        val nodes = parse("if (a) { }")

        assertTrue(blockOf(nodes[0]).nodes.isEmpty())
    }

    @Test
    fun `a closing brace without an open block is rejected`() {
        val exception = assertThrows<ParserException> { parse("println(\"a\");\n}").toList() }

        assertEquals("se encontró un \"}\" que no cierra ningún bloque", exception.message)
        assertEquals(1, exception.startPosition?.row)
    }

    @Test
    fun `else if is rejected instead of being parsed as a plain else`() {
        // El splitter corta antes de llegar acá, así que se arma la sentencia a mano para
        // cubrir el rechazo de la factory.
        val tokens =
            listOf(
                token(TokenType.CONDITIONAL, "if"),
                token(TokenType.PARENTHESIS, "("),
                token(TokenType.IDENTIFIER, "a"),
                token(TokenType.PARENTHESIS, ")"),
                token(TokenType.PUNCTUATOR, "{"),
                token(TokenType.PUNCTUATOR, "}"),
                token(TokenType.CONDITIONAL, "else"),
                token(TokenType.CONDITIONAL, "if"),
                token(TokenType.PARENTHESIS, "("),
                token(TokenType.IDENTIFIER, "b"),
                token(TokenType.PARENTHESIS, ")"),
                token(TokenType.PUNCTUATOR, "{"),
                token(TokenType.PUNCTUATOR, "}"),
            )

        val exception = assertThrows<ParserException> { ConditionalFactory().createAST(tokens) }

        assertTrue(exception.message!!.contains("else if"), exception.message)
    }
}
