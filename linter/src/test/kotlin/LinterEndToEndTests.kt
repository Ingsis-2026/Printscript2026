import ast.Tokenizer
import lexer.Lexer
import lexer.TokenMapper
import linter.Linter
import linter.LinterVersion
import org.junit.jupiter.api.Test
import parser.Parser
import rules.CamelCaseRule
import rules.InputOnlyRule
import rules.PrintOnlyRule
import rules.Rule
import rules.SnakeCaseRule
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Reglas ejercitadas de punta a punta: código fuente -> Lexer -> Parser -> Tokenizer -> Rule.
 *
 * Los tests de [RulesTests] construyen los tokens a mano, por lo que no detectan
 * desalineaciones entre lo que emite el Tokenizer y lo que buscan las reglas.
 */
class LinterEndToEndTests {
    private val lexer = Lexer(TokenMapper("1.1"))
    private val parser = Parser()
    private val tokenizer = Tokenizer()

    private fun violationsOf(
        rule: Rule,
        source: String,
    ) = rule.applyRule(tokenizer.parseToTokens(parser.execute(lexer.execute(source))))

    // --- PrintOnlyRule ---

    @Test
    fun `printOnly flags println called with an operation`() {
        val violations = violationsOf(PrintOnlyRule(), "println(1 + 2);")
        assertTrue(violations.isNotEmpty(), "println(1 + 2) debe violar PrintOnly")
    }

    @Test
    fun `printOnly allows println of a plain literal`() {
        val violations = violationsOf(PrintOnlyRule(), "println(5);")
        assertTrue(violations.isEmpty(), "println(5) no es una expresión: no debe violar PrintOnly")
    }

    @Test
    fun `printOnly allows println of a plain identifier`() {
        val violations = violationsOf(PrintOnlyRule(), "let x : number = 5;\nprintln(x);")
        assertTrue(violations.isEmpty(), "println(x) no es una expresión: no debe violar PrintOnly")
    }

    // --- InputOnlyRule ---

    @Test
    fun `inputOnly flags readInput called with an operation`() {
        val violations = violationsOf(InputOnlyRule(), "let x : string = readInput(\"a\" + \"b\");")
        assertTrue(violations.isNotEmpty(), "readInput(\"a\" + \"b\") debe violar InputOnly")
    }

    @Test
    fun `inputOnly allows readInput of a plain literal`() {
        val violations = violationsOf(InputOnlyRule(), "let x : string = readInput(\"name\");")
        assertTrue(violations.isEmpty(), "readInput(\"name\") no es una expresión: no debe violar InputOnly")
    }

    // --- Reglas de nombres ---

    @Test
    fun `camelCase flags a snake_case identifier`() {
        val violations = violationsOf(CamelCaseRule(), "let my_variable : number = 5;")
        assertTrue(violations.isNotEmpty(), "my_variable debe violar CamelCase")
    }

    @Test
    fun `camelCase allows a camelCase identifier`() {
        val violations = violationsOf(CamelCaseRule(), "let myVariable : number = 5;")
        assertTrue(violations.isEmpty(), "myVariable no debe violar CamelCase")
    }

    @Test
    fun `camelCase flags a PascalCase identifier`() {
        val violations = violationsOf(CamelCaseRule(), "let MyVariable : number = 5;")
        assertTrue(violations.isNotEmpty(), "MyVariable no empieza en minúscula: debe violar CamelCase")
    }

    @Test
    fun `snakeCase flags a camelCase identifier`() {
        val violations = violationsOf(SnakeCaseRule(), "let myVariable : number = 5;")
        assertTrue(violations.isNotEmpty(), "myVariable debe violar SnakeCase")
    }

    @Test
    fun `snakeCase allows a snake_case identifier`() {
        val violations = violationsOf(SnakeCaseRule(), "let my_variable : number = 5;")
        assertTrue(violations.isEmpty(), "my_variable no debe violar SnakeCase")
    }

    // --- Reutilización: las reglas no deben acumular estado entre corridas ---

    @Test
    fun `applyRule twice on the same rule instance does not duplicate violations`() {
        val rule = CamelCaseRule()
        val tokens = tokenizer.parseToTokens(parser.execute(lexer.execute("let my_variable : number = 5;")))

        val first = rule.applyRule(tokens).size
        val second = rule.applyRule(tokens).size

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
