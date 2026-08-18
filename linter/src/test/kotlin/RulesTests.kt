import ast.Tokenizer
import lexer.Lexer
import lexer.TokenMapper
import linter.BrokenRule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import parser.Parser
import rules.CamelCaseRule
import rules.InputOnlyRule
import rules.PrintOnlyRule
import rules.Rule
import rules.RuleValidator
import rules.SnakeCaseRule
import token.Token
import token.TokenPosition
import token.TokenType
import kotlin.test.assertFalse

class RulesTests {
    private val tokenMapper = TokenMapper("1.1")
    private val lexer = Lexer(tokenMapper)
    private val parser = Parser()
    private val tokenizer = Tokenizer()

    @Test
    fun `test identifier is in camel case`() {
        val input = "let myVariable:string = \"this is a variable\";"
        val tokens = lexer.execute(input)
        val trees = parser.execute(tokens)
        val tokensList = tokenizer.parseToTokens(trees)

        val camelCaseRule = CamelCaseRule()
        val brokenRules = camelCaseRule.applyRule(tokensList)

        assertTrue(brokenRules.isEmpty())
    }

    @Test
    fun `getRuleName should return the correct rule name2`() {
        val camelCaseRule = CamelCaseRule()
        val ruleName = camelCaseRule.getRuleName()

        assertEquals("CamelCase", ruleName, "The rule name should be 'CamelCase'")
    }

    @Test
    fun `getRuleDescription should return the correct default rule description3`() {
        val camelCaseRule = CamelCaseRule()
        val ruleDescription = camelCaseRule.getRuleDescription()

        assertEquals(
            "The following identifier must be in camel case",
            ruleDescription,
            "The default rule description should match the expected message",
        )
    }

    @Test
    fun `getRuleDescription should return the customized rule description if provided2`() {
        val customMessage = "Identifiers should follow camel case convention"
        val camelCaseRule = CamelCaseRule(customMessage)
        val ruleDescription = camelCaseRule.getRuleDescription()

        assertEquals(customMessage, ruleDescription, "The rule description should match the custom error message provided")
    }

    @Test
    fun `test identifier is in snake case`() {
        val input = "let my_variable:string = \"this is a variable\";"
        val tokens = lexer.execute(input)
        val trees = parser.execute(tokens)
        val tokensList = tokenizer.parseToTokens(trees)

        val snakeCaseRule = SnakeCaseRule()
        val brokenRules = snakeCaseRule.applyRule(tokensList)

        assertTrue(brokenRules.isEmpty())
    }

    @Test
    fun `getRuleName should return the correct rule name4`() {
        val inputOnlyRule = InputOnlyRule()
        val ruleName = inputOnlyRule.getRuleName()

        assertEquals("InputOnly", ruleName, "The rule name should be 'InputOnly'")
    }

    @Test
    fun `getRuleDescription should return the correct default rule description`() {
        val inputOnlyRule = InputOnlyRule()
        val ruleDescription = inputOnlyRule.getRuleDescription()

        assertEquals(
            "ReadInputs must not be called with an expression",
            ruleDescription,
            "The default rule description should match the expected message",
        )
    }

    @Test
    fun `getRuleDescription should return the customized rule description if provided`() {
        val customMessage = "Custom error: InputOnly rule violation"
        val inputOnlyRule = InputOnlyRule(customMessage)
        val ruleDescription = inputOnlyRule.getRuleDescription()

        assertEquals(customMessage, ruleDescription, "The rule description should match the custom error message provided")
    }

    @Test
    fun `test println called without expression passes PrintOnlyRule`() {
        val input = "println(my_variable);"
        val tokens = lexer.execute(input)
        val trees = parser.execute(tokens)
        val tokensList = tokenizer.parseToTokens(trees)

        val printOnlyRule = PrintOnlyRule()
        val brokenRules = printOnlyRule.applyRule(tokensList)

        assertTrue(brokenRules.isEmpty())
    }

    @Test
    fun `getRuleName should return the correct rule name`() {
        val rule = InputOnlyRule()
        assertEquals("InputOnly", rule.getRuleName(), "Rule name should be 'InputOnly'")
    }

    @Test
    fun `getRuleDescription should return the default rule description`() {
        val rule = InputOnlyRule()
        assertEquals(
            "ReadInputs must not be called with an expression",
            rule.getRuleDescription(),
            "The default rule description should match the expected message",
        )
    }

    @Test
    fun `getRuleDescription should return custom rule description when set`() {
        val customMessage = "Custom rule description"
        val rule = InputOnlyRule(customMessage)
        assertEquals(customMessage, rule.getRuleDescription(), "The rule description should match the custom message")
    }

    @Test
    fun `containsReadInput should return true when 'inputonly' function token is present`() {
        val rule = InputOnlyRule()
        val tokens = listOf(Token(TokenType.FUNCTION, "inputonly", TokenPosition(0, 0), TokenPosition(0, 9)))
        assertTrue(rule.containsReadInput(tokens), "Expected to find 'inputonly' function token")
    }

    @Test
    fun `containsReadInput should return false when 'inputonly' function token is absent`() {
        val rule = InputOnlyRule()
        val tokens = listOf(Token(TokenType.FUNCTION, "otherFunction", TokenPosition(0, 0), TokenPosition(0, 12)))
        assertFalse(rule.containsReadInput(tokens), "Expected not to find 'inputonly' function token")
    }

    @Test
    fun `containsExpression should return true when tokens contain expression type`() {
        val rule = InputOnlyRule()
        val tokens = listOf(Token(TokenType.OPERATOR, "+", TokenPosition(1, 1), TokenPosition(1, 2)))
        assertTrue(rule.containsExpression(tokens), "Expected to find expression type in tokens")
    }

    @Test
    fun `containsExpression should return false when tokens contain only identifiers, punctuators, or literals`() {
        val rule = InputOnlyRule()
        val tokens =
            listOf(
                Token(TokenType.IDENTIFIER, "varName", TokenPosition(0, 1), TokenPosition(0, 8)),
                Token(TokenType.PUNCTUATOR, ";", TokenPosition(0, 8), TokenPosition(0, 9)),
                Token(TokenType.LITERAL, "5", TokenPosition(0, 10), TokenPosition(0, 11)),
            )
        assertFalse(rule.containsExpression(tokens), "Expected not to find expression type in tokens")
    }

    @Test
    fun `applyRule should return no broken rules when 'inputonly' is not followed by expression`() {
        val rule = InputOnlyRule()
        val tokens =
            listOf(
                listOf(
                    Token(TokenType.FUNCTION, "inputonly", TokenPosition(3, 1), TokenPosition(3, 10)),
                    Token(TokenType.IDENTIFIER, "variable", TokenPosition(3, 11), TokenPosition(3, 19)),
                ),
            )
        val brokenRules = rule.applyRule(tokens)
        assertTrue(brokenRules.isEmpty(), "Expected no broken rules")
    }

    @Test
    fun `applyRule should return no broken rules when 'println' is not followed by expression`() {
        val rule = PrintOnlyRule()
        val tokens =
            listOf(
                listOf(
                    Token(TokenType.FUNCTION, "println", TokenPosition(1, 0), TokenPosition(1, 7)),
                    Token(TokenType.IDENTIFIER, "message", TokenPosition(1, 8), TokenPosition(1, 15)),
                ),
            )
        val brokenRules = rule.applyRule(tokens)
        assertTrue(brokenRules.isEmpty(), "Expected no broken rules")
    }

    @Test
    fun `containsPrintln should return true when 'println' function token is present`() {
        val rule = PrintOnlyRule()
        val tokens = listOf(Token(TokenType.FUNCTION, "println", TokenPosition(0, 0), TokenPosition(0, 7)))
        assertTrue(rule.containsPrintln(tokens), "Expected to find 'println' function token")
    }

    @Test
    fun `containsPrintln should return false when 'println' function token is absent`() {
        val rule = PrintOnlyRule()
        val tokens = listOf(Token(TokenType.FUNCTION, "otherFunction", TokenPosition(0, 0), TokenPosition(0, 12)))
        assertFalse(rule.containsPrintln(tokens), "Expected not to find 'println' function token")
    }

    @Test
    fun `containsExpression should return true when tokens contain expression type2`() {
        val rule = PrintOnlyRule()
        val tokens = listOf(Token(TokenType.OPERATOR, "+", TokenPosition(1, 1), TokenPosition(1, 2)))
        assertTrue(rule.containsExpression(tokens), "Expected to find expression type in tokens")
    }

    @Test
    fun `containsExpression should return false when tokens contain only identifiers, punctuators, or literals2`() {
        val rule = PrintOnlyRule()
        val tokens =
            listOf(
                Token(TokenType.IDENTIFIER, "varName", TokenPosition(0, 1), TokenPosition(0, 8)),
                Token(TokenType.PUNCTUATOR, ";", TokenPosition(0, 8), TokenPosition(0, 9)),
                Token(TokenType.LITERAL, "5", TokenPosition(0, 10), TokenPosition(0, 11)),
            )
        assertFalse(rule.containsExpression(tokens), "Expected not to find expression type in tokens")
    }

    @Test
    fun `getRuleName should return the correct rule name3`() {
        val rule = PrintOnlyRule()
        val ruleName = rule.getRuleName()
        assertEquals("PrintOnly", ruleName, "The rule name should be 'PrintOnly'")
    }

    @Test
    fun `getRuleDescription should return the correct error message`() {
        val rule = PrintOnlyRule()
        val ruleDescription = rule.getRuleDescription()
        assertEquals(
            "Println must not be called with an expression",
            ruleDescription,
            "The rule description should match the error message",
        )
    }

    @Test
    fun `checkRule should apply multiple rules correctly`() {
        val rule1 =
            object : Rule {
                override fun applyRule(tokens: List<List<Token>>): List<BrokenRule> {
                    return if (tokens.isNotEmpty()) listOf(BrokenRule("Rule 1 violation", TokenPosition(0, 0))) else emptyList()
                }

                override fun getRuleName() = "Rule1"

                override fun getRuleDescription() = "Description for Rule 1"
            }

        val rule2 =
            object : Rule {
                override fun applyRule(tokens: List<List<Token>>): List<BrokenRule> {
                    return if (tokens.size > 1) listOf(BrokenRule("Rule 2 violation", TokenPosition(1, 0))) else emptyList()
                }

                override fun getRuleName() = "Rule2"

                override fun getRuleDescription() = "Description for Rule 2"
            }

        val tokens =
            listOf(
                listOf(Token(TokenType.IDENTIFIER, "x", TokenPosition(0, 0), TokenPosition(0, 1))),
                listOf(Token(TokenType.IDENTIFIER, "y", TokenPosition(1, 0), TokenPosition(1, 1))),
            )

        val validator = RuleValidator()
        val brokenRules = validator.checkRule(listOf(rule1, rule2), tokens)

        assertEquals(2, brokenRules.size, "Expected two broken rules")
        assertTrue(brokenRules.any { it.ruleDescription == "Rule 1 violation" }, "Expected to find Rule 1 violation")
        assertTrue(brokenRules.any { it.ruleDescription == "Rule 2 violation" }, "Expected to find Rule 2 violation")
    }
}
