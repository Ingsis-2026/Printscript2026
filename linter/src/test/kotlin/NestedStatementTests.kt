import ast.ASTNode
import lexer.Lexer
import lexer.TokenMapper
import linter.BrokenRule
import linter.Linter
import linter.LinterVersion
import org.junit.jupiter.api.Test
import parser.Parser
import token.TokenPosition
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Las sentencias de adentro de un bloque se revisan como cualquier otra.
 *
 * Antes no ocurría: el linter aplanaba el `if` entero —bloques incluidos— en una sola fila de
 * tokens, así que la regla de println, que miraba el primer token de la fila, nunca podía
 * activarse adentro de un bloque. Recorrer el árbol lo corrige, y cada violación queda
 * informada en la sentencia que la contiene y no en el `if`.
 */
class NestedStatementTests {
    @Test
    fun `un println con expresión adentro de un bloque se informa en el println`() {
        val violations = violationsOf("if (true) { println(1 + 2); }", PRINT_ONLY)

        assertEquals(1, violations.size)
        assertEquals(TokenPosition(0, 12), violations.single().errorPosition)
    }

    @Test
    fun `un readInput con expresión adentro de un bloque se informa en su declaración`() {
        val violations = violationsOf("""if (true) { let a: string = readInput("a" + "b"); }""", INPUT_ONLY)

        assertEquals(1, violations.size)
        assertEquals(TokenPosition(0, 16), violations.single().errorPosition)
    }

    @Test
    fun `la condición del if también se revisa`() {
        val violations = violationsOf("if (my_flag) { let x: number = 1; }", CAMEL_CASE)

        assertEquals(1, violations.size)
        assertEquals(TokenPosition(0, 4), violations.single().errorPosition)
    }

    @Test
    fun `un nombre adentro de un bloque se informa una sola vez`() {
        val violations = violationsOf("if (flag) { let my_var: number = 1; }", CAMEL_CASE)

        assertEquals(1, violations.size)
        assertEquals(TokenPosition(0, 16), violations.single().errorPosition)
    }

    @Test
    fun `cada sentencia del bloque se revisa por separado`() {
        val violations = violationsOf("if (flag) { let a_b: number = 1; let c_d: number = 2; }", CAMEL_CASE)

        assertEquals(2, violations.size)
    }

    @Test
    fun `el bloque del else también se revisa`() {
        val violations = violationsOf("if (flag) { let x: number = 1; } else { println(1 + 2); }", PRINT_ONLY)

        assertTrue(violations.isNotEmpty(), "el println del else debe violar la regla")
    }

    private fun violationsOf(
        source: String,
        config: String,
    ): List<BrokenRule> {
        val linter = Linter.forConfig(requireNotNull(LinterVersion.fromString(VERSION)), config)
        return linter.check(nodesOf(source)).getBrokenRules()
    }

    private fun nodesOf(source: String): List<ASTNode> = Parser.forVersion(VERSION).execute(Lexer(TokenMapper(VERSION)).execute(source))

    private companion object {
        const val VERSION = "1.1"
        const val CAMEL_CASE = """{"identifier_format": "camelcase"}"""
        const val PRINT_ONLY = """{"mandatory-variable-or-literal-in-println": true}"""
        const val INPUT_ONLY = """{"mandatory-variable-or-literal-in-readInput": true}"""
    }
}
