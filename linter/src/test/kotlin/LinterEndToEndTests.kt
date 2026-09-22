import lexer.Lexer
import lexer.TokenMapper
import linter.Linter
import linter.LinterVersion
import org.junit.jupiter.api.Test
import parser.Parser
import rules.CallArgumentRule
import rules.IdentifierFormat
import rules.IdentifierFormatRule
import rules.Rule
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Reglas ejercitadas de punta a punta: código fuente -> Lexer -> Parser -> Rule.
 *
 * Al partir del fuente y no de nodos construidos a mano, cualquier desalineación entre lo que
 * arma el parser y lo que buscan las reglas queda a la vista.
 */
class LinterEndToEndTests {
    private val lexer = Lexer(TokenMapper("1.1"))
    private val parser = Parser()

    private fun violationsOf(
        rule: Rule,
        source: String,
    ) = parser.execute(lexer.execute(source)).flatMap { rule.check(it) }

    private fun printOnly() = CallArgumentRule("println", "Println must not be called with an expression")

    private fun inputOnly() = CallArgumentRule("readInput", "ReadInputs must not be called with an expression")

    // --- PrintOnlyRule ---

    @Test
    fun `printOnly flags println called with an operation`() {
        val violations = violationsOf(printOnly(), "println(1 + 2);")
        assertTrue(violations.isNotEmpty(), "println(1 + 2) debe violar PrintOnly")
    }

    @Test
    fun `printOnly allows println of a plain literal`() {
        val violations = violationsOf(printOnly(), "println(5);")
        assertTrue(violations.isEmpty(), "println(5) no es una expresión: no debe violar PrintOnly")
    }

    @Test
    fun `printOnly allows println of a plain identifier`() {
        val violations = violationsOf(printOnly(), "let x : number = 5;\nprintln(x);")
        assertTrue(violations.isEmpty(), "println(x) no es una expresión: no debe violar PrintOnly")
    }

    // --- InputOnlyRule ---

    @Test
    fun `inputOnly flags readInput called with an operation`() {
        val violations = violationsOf(inputOnly(), "let x : string = readInput(\"a\" + \"b\");")
        assertTrue(violations.isNotEmpty(), "readInput(\"a\" + \"b\") debe violar InputOnly")
    }

    @Test
    fun `inputOnly allows readInput of a plain literal`() {
        val violations = violationsOf(inputOnly(), "let x : string = readInput(\"name\");")
        assertTrue(violations.isEmpty(), "readInput(\"name\") no es una expresión: no debe violar InputOnly")
    }

    // --- Reglas de nombres ---

    @Test
    fun `camelCase flags a snake_case identifier`() {
        val violations = violationsOf(IdentifierFormatRule(IdentifierFormat.CAMEL_CASE), "let my_variable : number = 5;")
        assertTrue(violations.isNotEmpty(), "my_variable debe violar CamelCase")
    }

    @Test
    fun `camelCase allows a camelCase identifier`() {
        val violations = violationsOf(IdentifierFormatRule(IdentifierFormat.CAMEL_CASE), "let myVariable : number = 5;")
        assertTrue(violations.isEmpty(), "myVariable no debe violar CamelCase")
    }

    @Test
    fun `camelCase flags a PascalCase identifier`() {
        val violations = violationsOf(IdentifierFormatRule(IdentifierFormat.CAMEL_CASE), "let MyVariable : number = 5;")
        assertTrue(violations.isNotEmpty(), "MyVariable no empieza en minúscula: debe violar CamelCase")
    }

    @Test
    fun `snakeCase flags a camelCase identifier`() {
        val violations = violationsOf(IdentifierFormatRule(IdentifierFormat.SNAKE_CASE), "let myVariable : number = 5;")
        assertTrue(violations.isNotEmpty(), "myVariable debe violar SnakeCase")
    }

    @Test
    fun `snakeCase allows a snake_case identifier`() {
        val violations = violationsOf(IdentifierFormatRule(IdentifierFormat.SNAKE_CASE), "let my_variable : number = 5;")
        assertTrue(violations.isEmpty(), "my_variable no debe violar SnakeCase")
    }

    // --- Reutilización: las reglas no deben acumular estado entre corridas ---

    @Test
    fun `check twice on the same rule instance does not duplicate violations`() {
        val rule = IdentifierFormatRule(IdentifierFormat.CAMEL_CASE)
        val statements = parser.execute(lexer.execute("let my_variable : number = 5;"))

        val first = statements.flatMap { rule.check(it) }.size
        val second = statements.flatMap { rule.check(it) }.size

        assertEquals(first, second, "Reaplicar la regla no debe acumular violaciones previas")
    }

    @Test
    fun `check twice on the same linter does not duplicate violations`() {
        val linter = Linter(LinterVersion.VERSION_1_0)
        linter.readJson("""{"identifier_format": "camelCase"}""")
        val trees = parser.execute(lexer.execute("let my_variable : number = 5;"))

        val first = linter.check(trees).getBrokenRules().size
        val second = linter.check(trees).getBrokenRules().size

        assertFalse(first == 0, "El caso de prueba requiere al menos una violación")
        assertEquals(first, second, "Reejecutar check no debe acumular violaciones previas")
    }
}
