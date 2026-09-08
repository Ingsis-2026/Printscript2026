package formatter

/**
 * Lo que hay que saber del recorrido para decidir un hueco: en qué bloque estamos, qué
 * sentencia se está leyendo y con cuánta sangría empezó la línea en curso.
 *
 * Se actualiza *después* de resolver el separador que precede a cada token, así que mientras
 * se decide el hueco que sigue a un ";" el estado todavía describe la sentencia que ese ";"
 * terminó. De ahí que la regla de saltos después de un println pueda consultarla.
 */
internal class FormattingState {
    var depth: Int = 0
        private set

    var inDeclaration: Boolean = false
        private set

    var statementWasPrintln: Boolean = false
        private set

    var currentLineIndent: Int = 0
        private set

    private var atStatementStart = true
    private var lastRow = -1

    fun advance(token: SourceToken) {
        if (token.startRow != lastRow) {
            lastRow = token.startRow
            currentLineIndent = token.startColumn
        }

        if (atStatementStart) {
            statementWasPrintln = token.value == PRINTLN
            inDeclaration = token.value in DECLARATION_KEYWORDS
            atStatementStart = false
        }

        when {
            token.isOpeningBrace -> {
                depth += 1
                startStatement()
            }

            token.isClosingBrace -> {
                depth -= 1
                startStatement()
            }

            token.isSemicolon -> startStatement()
        }
    }

    private fun startStatement() {
        atStatementStart = true
        inDeclaration = false
    }

    private companion object {
        const val PRINTLN = "println"
        val DECLARATION_KEYWORDS = setOf("let", "const")
    }
}
