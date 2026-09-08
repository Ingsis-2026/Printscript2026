import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import rules.FormattingRules
import rules.RulesReader

/**
 * La configuración llega en JSON o en YAML y habilita una regla por archivo, así que lo que
 * más importa es que una clave ausente quede apagada en lugar de fallar.
 */
class RulesReaderTest {
    private val reader = RulesReader()

    @Test
    fun `reads the json the tck provides`() {
        val rules = reader.read("""{ "indent-inside-if": 4 }""")

        assertEquals(4, rules.indentInsideIf)
        assertFalse(rules.singleSpaceSeparation)
        assertNull(rules.spaceAroundEquals)
    }

    @Test
    fun `reads yaml as well`() {
        val rules = reader.read("mandatory-single-space-separation: true\nline-breaks-after-println: 2")

        assertTrue(rules.singleSpaceSeparation)
        assertEquals(2, rules.lineBreaksAfterPrintln)
    }

    @Test
    fun `an empty configuration enables nothing`() {
        assertEquals(FormattingRules(), reader.read("{}"))
        assertEquals(FormattingRules(), reader.read(""))
    }

    @Test
    fun `unknown keys are ignored`() {
        assertEquals(FormattingRules(), reader.read("""{ "some-rule-we-do-not-have": true }"""))
    }

    @Test
    fun `the two spellings of the equals rule are the same setting`() {
        assertEquals(true, reader.read("""{ "enforce-spacing-around-equals": true }""").spaceAroundEquals)
        assertEquals(false, reader.read("""{ "enforce-no-spacing-around-equals": true }""").spaceAroundEquals)
    }

    @Test
    fun `the two spellings of the brace rule are the same setting`() {
        assertEquals(true, reader.read("""{ "if-brace-same-line": true }""").braceOnSameLine)
        assertEquals(false, reader.read("""{ "if-brace-below-line": true }""").braceOnSameLine)
    }

    @Test
    fun `a rule set to false is not enabled`() {
        assertFalse(reader.read("""{ "mandatory-single-space-separation": false }""").singleSpaceSeparation)
    }

    @Test
    fun `reads a configuration file from disk`() {
        val rules = reader.readFile("src/test/resources/rules11.yaml")

        assertEquals(4, rules.indentInsideIf)
        assertTrue(rules.spaceAfterColon)
    }
}
