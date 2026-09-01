package cli

import cli.commands.AnalyzingCommand
import cli.commands.CommandStatus
import cli.commands.ExecutionCommand
import cli.commands.FormattingCommand
import cli.commands.ValidationCommand
import cli.io.OutputPrinter
import cli.pipeline.ParsingPipeline
import formatter.FormatterBuilderPS
import linter.Linter
import linter.LinterVersion
import org.junit.jupiter.api.Test
import parser.ParserException
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class CommandTest {
    private val output = CapturingOutput()

    private fun pipeline(version: String = "1.0") = ParsingPipeline(version)

    @Test
    fun `validation informa la cantidad de sentencias`() {
        val source = InMemorySource("prueba.ps", "let x : number = 1;\nprintln(x);")

        val status = ValidationCommand(source, pipeline(), output).execute()

        assertEquals(CommandStatus.SUCCESS, status)
        assertTrue(output.infoText().contains("Validación exitosa: 2 sentencias en prueba.ps"))
    }

    @Test
    fun `validation propaga el error de parseo con su ubicacion`() {
        val source = InMemorySource("roto.ps", "println(5)")

        val exception = assertFailsWith<ParserException> { ValidationCommand(source, pipeline(), output).execute() }

        assertEquals(0, exception.startPosition?.row)
        assertTrue(exception.describe().contains("línea 1"))
    }

    @Test
    fun `validation concuerda el numero con la cantidad de sentencias`() {
        ValidationCommand(InMemorySource("una.ps", "let x : number = 1;"), pipeline(), output).execute()

        assertTrue(output.infoText().contains("Validación exitosa: 1 sentencia en una.ps"))
    }

    @Test
    fun `analyzing concuerda el numero con la cantidad de problemas`() {
        val source = InMemorySource("uno.ps", "let Uno : number = 1;")

        AnalyzingCommand(source, pipeline(), linter("linterRules.json", LinterVersion.VERSION_1_0), output).execute()

        assertTrue(output.errorText().contains("Se encontraron 1 problema en uno.ps"))
    }

    @Test
    fun `validation no escribe en el canal de resultado`() {
        ValidationCommand(InMemorySource("p.ps", "let x : number = 1;"), pipeline(), output).execute()

        assertTrue(output.results.isEmpty())
    }

    @Test
    fun `execution imprime la salida del programa en el canal de resultado`() {
        val source = InMemorySource("ejemplo1.ps", File("src/test/resources/example1.ps").readText())

        val status = execution(source).execute()

        assertEquals(CommandStatus.SUCCESS, status)
        assertEquals("Joe Doe", output.resultText())
    }

    @Test
    fun `execution resuelve los tres ejemplos de la consigna`() {
        val expected = mapOf("example1.ps" to "Joe Doe", "example2.ps" to "Result: 3", "example3.ps" to "Result: 3")

        expected.forEach { (fileName, expectedOutput) ->
            val commandOutput = CapturingOutput()
            val source = InMemorySource(fileName, File("src/test/resources/$fileName").readText())
            ExecutionCommand(source, pipeline(), "1.0", OutputPrinter(commandOutput), ScriptedReader(), commandOutput).execute()

            assertEquals(expectedOutput, commandOutput.resultText(), "salida inesperada para $fileName")
        }
    }

    @Test
    fun `execution lee la entrada del usuario con readInput en 1 punto 1`() {
        val source = InMemorySource("entrada.ps", "let x : string = readInput(\"nombre: \");\nprintln(x);")

        ExecutionCommand(source, pipeline("1.1"), "1.1", OutputPrinter(output), ScriptedReader(listOf("Ana")), output).execute()

        assertTrue(output.results.contains("Ana"))
    }

    @Test
    fun `formatting entrega cada sentencia formateada al destino`() {
        val sink = RecordingSink()
        val formatter = FormatterBuilderPS().build("src/test/resources/formatterRules.yaml", "1.0")
        val source = InMemorySource("p.ps", "let x:number=8;")

        val status = FormattingCommand(source, formatter, sink, output).execute()

        assertEquals(CommandStatus.SUCCESS, status)
        assertEquals(listOf("let x: number = 8;"), sink.statements)
        assertTrue(output.infoText().contains("Formateo finalizado"))
    }

    @Test
    fun `analyzing informa cada regla incumplida con su ubicacion`() {
        val source = InMemorySource("ejemplo1.ps", File("src/test/resources/example1.ps").readText())

        val status = AnalyzingCommand(source, pipeline(), linter("linterRules.json", LinterVersion.VERSION_1_0), output).execute()

        assertEquals(CommandStatus.FAILURE, status)
        assertTrue(output.errorText().contains("Se encontraron 1 problema en ejemplo1.ps"))
        assertTrue(output.errorText().contains("(línea 4, columna 1)"))
    }

    @Test
    fun `analyzing termina bien cuando el fuente cumple las reglas`() {
        val source = InMemorySource("limpio.ps", "let unaVariable : number = 8;")

        val status = AnalyzingCommand(source, pipeline(), linter("linterRules.json", LinterVersion.VERSION_1_0), output).execute()

        assertEquals(CommandStatus.SUCCESS, status)
        assertTrue(output.infoText().contains("No se encontraron problemas en limpio.ps"))
    }

    @Test
    fun `analyzing pluraliza el reporte`() {
        val source = InMemorySource("varios.ps", "let Uno : number = 1;\nlet Dos : number = 2;")

        AnalyzingCommand(source, pipeline(), linter("linterRules.json", LinterVersion.VERSION_1_0), output).execute()

        assertTrue(output.errorText().contains("Se encontraron 2 problemas"))
    }

    private fun execution(source: InMemorySource) =
        ExecutionCommand(source, pipeline(), "1.0", OutputPrinter(output), ScriptedReader(), output)

    private fun linter(
        fileName: String,
        version: LinterVersion,
    ): Linter {
        val linter = Linter(version)
        linter.readJson(File("src/test/resources/$fileName").readText())
        return linter
    }
}
