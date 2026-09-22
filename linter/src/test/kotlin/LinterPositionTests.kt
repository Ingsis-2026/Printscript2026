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
 * Dónde informa el linter cada violación.
 *
 * La posición de una violación es parte de lo que se le promete a quien usa la herramienta, y
 * no la fijaba ningún test: los demás sólo miran si hubo violaciones o no. Estos casos la fijan
 * pasando por la API pública, para que sobrevivan a cualquier cambio interno del módulo.
 */
class LinterPositionTests {
    @Test
    fun `readInput con expresión se informa en el nombre declarado, no en la llamada`() {
        val violations = violationsOf("""let code: string = readInput("a" + "b");""", INPUT_ONLY)

        assertEquals(1, violations.size)
        assertEquals(TokenPosition(0, 4), violations.single().errorPosition)
    }

    @Test
    fun `una declaración mal nombrada se informa en el identificador`() {
        val violations = violationsOf("let my_var: number = 5;", CAMEL_CASE)

        assertEquals(1, violations.size)
        assertEquals(TokenPosition(0, 4), violations.single().errorPosition)
    }

    @Test
    fun `una asignación mal nombrada se informa donde el parser ubica al nodo`() {
        val violations = violationsOf("myVar = 5;", SNAKE_CASE)

        assertEquals(1, violations.size)
        assertEquals(TokenPosition(0, 6), violations.single().errorPosition)
    }

    @Test
    fun `println con expresión se informa en el println`() {
        val violations = violationsOf("println(1 + 2);", PRINT_ONLY)

        assertEquals(1, violations.size)
        assertEquals(TokenPosition(0, 0), violations.single().errorPosition)
    }

    @Test
    fun `readInput con expresión en una asignación se informa donde el parser ubica al nodo`() {
        val violations = violationsOf("""code = readInput("a" + "b");""", INPUT_ONLY)

        assertEquals(1, violations.size)
        assertEquals(TokenPosition(0, 5), violations.single().errorPosition)
    }

    @Test
    fun `el uso de un nombre se informa en el uso y no en la declaración`() {
        val violations = violationsOf("let my_var: number = otherVar;", SNAKE_CASE)

        assertEquals(1, violations.size)
        assertEquals(TokenPosition(0, 21), violations.single().errorPosition)
    }

    @Test
    fun `camel case y snake case señalan nombres distintos del mismo fuente`() {
        val violations = violationsOf("let my_var: number = otherVar;", CAMEL_CASE)

        assertEquals(1, violations.size)
        assertEquals(TokenPosition(0, 4), violations.single().errorPosition)
    }

    @Test
    fun `las violaciones salen por sentencia y dentro de cada una por regla`() {
        val violations =
            violationsOf(
                "let my_var: number = 1;\nprintln(1 + 2);",
                """{"identifier_format": "camelcase", "mandatory-variable-or-literal-in-println": true}""",
            )

        assertEquals(
            listOf(TokenPosition(0, 4), TokenPosition(1, 0)),
            violations.map { it.errorPosition },
        )
    }

    @Test
    fun `una declaración sin valor no rompe ninguna regla de nombres`() {
        val violations = violationsOf("let x: number;", CAMEL_CASE)

        assertTrue(violations.isEmpty())
    }

    @Test
    fun `readEnv no es readInput`() {
        val violations = violationsOf("""let x: string = readEnv("a" + "b");""", INPUT_ONLY)

        assertTrue(violations.isEmpty())
    }

    private fun violationsOf(
        source: String,
        config: String,
    ): List<BrokenRule> {
        val linter = Linter(requireNotNull(LinterVersion.fromString(VERSION)))
        linter.readJson(config)
        return linter.check(nodesOf(source)).getBrokenRules()
    }

    private fun nodesOf(source: String): List<ASTNode> =
        Parser.forVersion(VERSION).execute(Lexer(TokenMapper(VERSION)).convertToTokens(source))

    private companion object {
        const val VERSION = "1.1"
        const val CAMEL_CASE = """{"identifier_format": "camelcase"}"""
        const val SNAKE_CASE = """{"identifier_format": "snakecase"}"""
        const val PRINT_ONLY = """{"mandatory-variable-or-literal-in-println": true}"""
        const val INPUT_ONLY = """{"mandatory-variable-or-literal-in-readInput": true}"""
    }
}
