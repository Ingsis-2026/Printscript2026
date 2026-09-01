package cli

import cli.io.FormattedSink
import cli.io.Output
import cli.io.Source
import cli.progress.ProgressReporter
import interpreter.Reader

/** Captura los tres canales de la CLI por separado, para poder afirmar sobre cada uno. */
class CapturingOutput : Output {
    val info = mutableListOf<String>()
    val results = mutableListOf<String>()
    val errors = mutableListOf<String>()

    override fun info(message: String) {
        info.add(message)
    }

    override fun result(message: String) {
        results.add(message)
    }

    override fun error(message: String) {
        errors.add(message)
    }

    fun infoText(): String = info.joinToString("\n")

    fun resultText(): String = results.joinToString("\n")

    fun errorText(): String = errors.joinToString("\n")
}

/** Entrada predefinida para `readInput`, sin tocar `stdin`. */
class ScriptedReader(
    private val inputs: List<String> = emptyList(),
) : Reader {
    private var next = 0

    override fun input(message: String): String = inputs.getOrElse(next++) { "" }
}

/** Fuente en memoria, para los tests que no necesitan un archivo real. */
class InMemorySource(
    override val name: String,
    private val text: String,
) : Source {
    override fun <T> useLines(block: (Sequence<String>) -> T): T = block(text.lineSequence())
}

/**
 * Fuente que cuenta cuántas líneas se le pidieron.
 *
 * Sirve para probar que la CLI procesa el fuente de a poco: si leyera todo antes de trabajar,
 * el contador quedaría en el total apenas empezara.
 */
class CountingSource(
    private val lineCount: Int,
    override val name: String = "counting",
) : Source {
    var linesRead = 0
        private set

    override fun <T> useLines(block: (Sequence<String>) -> T): T =
        block(
            (0 until lineCount)
                .asSequence()
                .map {
                    linesRead++
                    "let x$it : number = $it;"
                },
        )
}

/** Retiene las sentencias formateadas en lugar de escribirlas a algún lado. */
class RecordingSink : FormattedSink {
    val statements = mutableListOf<String>()

    override fun write(statements: Sequence<String>) {
        statements.forEach { this.statements.add(it) }
    }
}

/** Registra el avance informado, para poder afirmar que acompaña al parseo. */
class RecordingProgressReporter : ProgressReporter {
    val counts = mutableListOf<Int>()
    var finishedAt: Int? = null
        private set

    override fun onStatementParsed(count: Int) {
        counts.add(count)
    }

    override fun onFinished(total: Int) {
        finishedAt = total
    }
}
