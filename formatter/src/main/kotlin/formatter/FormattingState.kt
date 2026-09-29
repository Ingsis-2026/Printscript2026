package formatter

/** Lo que se sabe de los tokens leídos hasta ahora. */
internal class FormattingState {
    var depth: Int = 0
        private set

    /** La sentencia del último token leído: un ";" o una llave pertenecen a la sentencia que cierran. */
    var statement: StatementKind = StatementKind.OTHER
        private set

    var currentLineIndent: Int = 0
        private set

    private var nextTokenStartsStatement = true
    private var lastRow = -1

    fun advance(token: SourceToken) {
        if (token.row != lastRow) {
            lastRow = token.row
            currentLineIndent = token.startColumn
        }

        if (nextTokenStartsStatement) statement = StatementKind.startedBy(token)
        nextTokenStartsStatement = token.isSemicolon || token.isOpeningBrace || token.isClosingBrace

        if (token.isOpeningBrace) depth += 1
        if (token.isClosingBrace) depth -= 1
    }
}
