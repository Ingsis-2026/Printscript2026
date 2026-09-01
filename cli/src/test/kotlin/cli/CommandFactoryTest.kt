package cli

import cli.arguments.ArgumentParser
import cli.commands.AnalyzingCommand
import cli.commands.CommandFactory
import cli.commands.CommandStatus
import cli.commands.ExecutionCommand
import cli.commands.FormattingCommand
import cli.commands.ValidationCommand
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class CommandFactoryTest {
    private val output = CapturingOutput()
    private val factory = CommandFactory(output, reader = ScriptedReader())
    private val parser = ArgumentParser()
    private val resources = "src/test/resources"

    private fun commandFor(arguments: List<String>) = factory.create(parser.parse(arguments))

    @Test
    fun `arma el comando de cada operacion`() {
        assertIs<ValidationCommand>(commandFor(listOf("validation", "$resources/example1.ps")))
        assertIs<ExecutionCommand>(commandFor(listOf("execution", "$resources/example1.ps")))
        assertIs<FormattingCommand>(
            commandFor(listOf("formatting", "$resources/example1.ps", "--config", "$resources/formatterRules.yaml")),
        )
        assertIs<AnalyzingCommand>(
            commandFor(listOf("analyzing", "$resources/example1.ps", "--config", "$resources/linterRules.json")),
        )
    }

    @Test
    fun `admite reglas de formateo en JSON y en YAML`() {
        listOf("formatterRules.json", "formatterRules.yaml").forEach { rules ->
            val command = commandFor(listOf("formatting", "$resources/test01.txt", "--config", "$resources/$rules"))

            assertEquals(CommandStatus.SUCCESS, command.execute(), "falló con $rules")
        }
    }

    @Test
    fun `rechaza una regla del linter que la version no soporta`() {
        val exception =
            assertFailsWith<IllegalArgumentException> {
                commandFor(listOf("analyzing", "$resources/example1.ps", "--config", "$resources/linterRules11.json"))
            }

        assertEquals("Rule not available for this version", exception.message)
    }

    @Test
    fun `el invocador delega la ejecucion al comando`() {
        val command = commandFor(listOf("validation", "$resources/test01.txt"))

        assertEquals(CommandStatus.SUCCESS, CliInvoker().runCommand(command))
    }
}
