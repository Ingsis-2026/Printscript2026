import ast.ASTNode
import linter.BrokenRule
import linter.Linter
import linter.LinterOutput
import linter.LinterVersion
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import token.TokenPosition
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LinterTests {
    private val version = LinterVersion.VERSION_1_0
    private val linter = Linter(version)

    @Test
    fun `check should return empty LinterOutput when no rules are broken`() {
        val astNodes = listOf<ASTNode>()

        val result = linter.check(astNodes)

        assertTrue(result.isCorrect, "LinterOutput should be correct when no rules are broken")
        assertTrue(result.getBrokenRules().isEmpty(), "No broken rules should be present")
    }

    @Test
    fun `test read JSON from file and load rules`() {
        val linter = Linter(LinterVersion.VERSION_1_1)
        val filePath = "src/test/resources/linter_rules.json"

        linter.readJson(File(filePath).readText())

        val loadedRules = linter.getRules()
        assertEquals(3, loadedRules.size)
    }

    @Test
    fun `the newest version still accepts a rule introduced in 1_1`() {
        // Sin versión mínima, una versión posterior a la que introdujo la regla la rechazaría.
        val linter = Linter(LinterVersion.entries.last())
        val filePath = "src/test/resources/linter_rules.json"

        linter.readJson(File(filePath).readText())

        assertEquals(3, linter.getRules().size)
    }

    @Test
    fun `test error when wrong version`() {
        val linter = Linter(LinterVersion.VERSION_1_0)
        val filePath = "src/test/resources/linter_rules.json"

        assertThrows<IllegalArgumentException> {
            linter.readJson(File(filePath).readText())
        }
    }

    @Test
    fun `test BrokenRule constructor`() {
        val ruleDescription = "Some error description"
        val errorPosition = TokenPosition(1, 5)
        val brokenRule = BrokenRule(ruleDescription, errorPosition)

        assertEquals(ruleDescription, brokenRule.ruleDescription)
        assertEquals(errorPosition, brokenRule.errorPosition)
    }

    @Test
    fun `test BrokenRule toString`() {
        val ruleDescription = "Invalid syntax"
        val errorPosition = TokenPosition(2, 10)
        val brokenRule = BrokenRule(ruleDescription, errorPosition)

        val expectedString = "Broken rule: $ruleDescription at $errorPosition"
        assertEquals(expectedString, brokenRule.toString())
    }

    @Test
    fun `test BrokenRule with different positions`() {
        val ruleDescription = "Missing semicolon"
        val errorPosition = TokenPosition(3, 15)
        val brokenRule = BrokenRule(ruleDescription, errorPosition)

        assertEquals("Broken rule: $ruleDescription at $errorPosition", brokenRule.toString())
    }

    @Test
    fun `test BrokenRule with negative position`() {
        val ruleDescription = "Unexpected token"
        val errorPosition = TokenPosition(-1, -1)
        val brokenRule = BrokenRule(ruleDescription, errorPosition)

        val expectedString = "Broken rule: $ruleDescription at $errorPosition"
        assertEquals(expectedString, brokenRule.toString())
    }

    @Test
    fun `test initial state of LinterOutput`() {
        val output = LinterOutput()
        assertTrue(output.isCorrect, "Initial state should be correct")
        assertTrue(output.getBrokenRules().isEmpty(), "No broken rules should be present initially")
    }

    @Test
    fun `test addBrokenRule updates state and stores broken rule`() {
        val output = LinterOutput()
        val brokenRule = BrokenRule("Example rule", TokenPosition(3, 5))

        output.addBrokenRule(brokenRule)

        assertFalse(output.isCorrect, "State should be incorrect after adding a broken rule")
        val brokenRules = output.getBrokenRules()
        assertEquals(1, brokenRules.size, "There should be one broken rule added")
        assertEquals("Example rule", brokenRules[0].ruleDescription, "The broken rule description should match")
        assertEquals(3, brokenRules[0].errorPosition.row, "Row number should match")
        assertEquals(5, brokenRules[0].errorPosition.column, "Column number should match")
    }

    @Test
    fun `test addMultipleBrokenRules`() {
        val output = LinterOutput()
        val rule1 = BrokenRule("First rule", TokenPosition(1, 1))
        val rule2 = BrokenRule("Second rule", TokenPosition(2, 2))

        output.addBrokenRule(rule1)
        output.addBrokenRule(rule2)

        assertFalse(output.isCorrect, "State should be incorrect after adding broken rules")
        val brokenRules = output.getBrokenRules()
        assertEquals(2, brokenRules.size, "There should be two broken rules added")

        // Check the details of each broken rule
        assertEquals("First rule", brokenRules[0].ruleDescription)
        assertEquals(1, brokenRules[0].errorPosition.row)
        assertEquals(1, brokenRules[0].errorPosition.column)

        assertEquals("Second rule", brokenRules[1].ruleDescription)
        assertEquals(2, brokenRules[1].errorPosition.row)
        assertEquals(2, brokenRules[1].errorPosition.column)
    }

    @Test
    fun `test getBrokenRules returns empty list if none added`() {
        val output = LinterOutput()
        val brokenRules = output.getBrokenRules()
        assertTrue(brokenRules.isEmpty(), "Should return an empty list when no broken rules are added")
    }

    @Test
    fun `test getBrokenRules returns correctly formatted rules`() {
        val output = LinterOutput()
        val brokenRule1 = BrokenRule("Rule one", TokenPosition(10, 20))
        val brokenRule2 = BrokenRule("Rule two", TokenPosition(15, 30))

        output.addBrokenRule(brokenRule1)
        output.addBrokenRule(brokenRule2)

        val brokenRules = output.getBrokenRules()

        assertEquals(2, brokenRules.size)
        assertEquals("Rule one", brokenRules[0].ruleDescription)
        assertEquals(TokenPosition(10, 20), brokenRules[0].errorPosition)
        assertEquals("Rule two", brokenRules[1].ruleDescription)
        assertEquals(TokenPosition(15, 30), brokenRules[1].errorPosition)
    }
}
