package formatter

/**
 * Lo que hay que saber del recorrido para decidir un hueco: en qué bloque estamos, qué
 * sentencia se está leyendo, cuál acaba de terminar y con cuánta sangría empezó la línea en curso.
 *
 * Se actualiza *después* de resolver el hueco que precede a cada token, así que el hueco que
 * sigue a un ";" se decide con [closedStatement] apuntando a la sentencia que ese ";" terminó.
 */
internal class FormattingState {
    var depth: Int = 0
        private set

    /** La sentencia que se está leyendo, o `null` entre un ";" o una llave y el token que sigue. */
    var currentStatement: StatementKind? = null
        private set

    /** La última sentencia que terminó en un ";" o en una llave. */
    var closedStatement: StatementKind? = null
        private set

    var currentLineIndent: Int = 0
        private set

    private var lastRow = -1

    fun advance(token: SourceToken) {
        if (token.startRow != lastRow) {
            lastRow = token.startRow
            currentLineIndent = token.startColumn
        }

        if (currentStatement == null) currentStatement = StatementKind.startedBy(token)

        if (token.isOpeningBrace) depth += 1
        if (token.isClosingBrace) depth -= 1

        if (token.isSemicolon || token.isOpeningBrace || token.isClosingBrace) {
            closedStatement = currentStatement
            currentStatement = null
        }
    }
}
