package cli

import cli.arguments.Operation
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OperationTest {
    @Test
    fun `reconoce las cuatro operaciones de la consigna`() {
        assertEquals(Operation.VALIDATION, Operation.fromArgument("validation"))
        assertEquals(Operation.EXECUTION, Operation.fromArgument("execution"))
        assertEquals(Operation.FORMATTING, Operation.fromArgument("formatting"))
        assertEquals(Operation.ANALYZING, Operation.fromArgument("analyzing"))
    }

    @Test
    fun `no distingue mayusculas de minusculas`() {
        assertEquals(Operation.EXECUTION, Operation.fromArgument("Execution"))
        assertEquals(Operation.ANALYZING, Operation.fromArgument("ANALYZING"))
    }

    @Test
    fun `devuelve null ante una operacion desconocida`() {
        assertNull(Operation.fromArgument("compilation"))
    }

    @Test
    fun `solo el formatter y el linter se configuran`() {
        assertTrue(Operation.FORMATTING.requiresConfig)
        assertTrue(Operation.ANALYZING.requiresConfig)
        assertFalse(Operation.VALIDATION.requiresConfig)
        assertFalse(Operation.EXECUTION.requiresConfig)
    }

    @Test
    fun `lista las operaciones validas para los mensajes de error`() {
        assertEquals("validation, execution, formatting, analyzing", Operation.supportedArguments())
    }
}
