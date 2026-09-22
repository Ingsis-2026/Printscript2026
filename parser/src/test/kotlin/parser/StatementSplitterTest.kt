package parser

import ast.BlockNode
import ast.ConditionalNode
import ast.DeclarationNode
import ast.PrintNode
import lexer.Lexer
import lexer.TokenMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** El splitter corta por el tipo del token, no por su texto. */
class StatementSplitterTest {
    private fun parse(
        version: String,
        source: String,
    ) = Parser.forVersion(version).execute(Lexer(TokenMapper(version)).execute(source))

    @Test
    fun `a string that reads like a brace or a semicolon does not cut the statement`() {
        val nodes = parse("1.1", "println(\"}\"); println(\";\"); println(\"{\");")

        assertEquals(3, nodes.size)
        assertTrue(nodes.all { it is PrintNode })
    }

    @Test
    fun `a string that reads like a brace inside a block stays in its statement`() {
        val nodes = parse("1.1", "if (a) { println(\"}\"); println(\"dos\"); }")

        val body = ((nodes.single() as ConditionalNode).thenBlock as BlockNode).nodes
        assertEquals(2, body.size)
    }

    @Test
    fun `in version 1,0 if and else can be variable names`() {
        val nodes = parse("1.0", "let if: number = 1;\nlet else: number = 2;\nprintln(if);")

        assertEquals(listOf("if", "else"), nodes.take(2).map { (it as DeclarationNode).id })
        assertTrue(nodes[2] is PrintNode)
    }
}
