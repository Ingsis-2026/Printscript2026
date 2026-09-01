package parser

import token.Token

/**
 * Agrupa la lista plana de tokens en sentencias (una lista de tokens por sentencia).
 *
 * Es una máquina de estados sobre el flujo de tokens: lleva la cuenta de las llaves
 * abiertas para no cortar dentro de un bloque, y recuerda si un `}` puede continuar
 * con un `else`. Cada tipo de token se resuelve en su propio método para mantener
 * acotada la complejidad.
 */
internal class StatementSplitter {
    private val rows = mutableListOf<List<Token>>()
    private var singleRow = mutableListOf<Token>()
    private var openBraces = 0
    private var expectingElse = false

    fun split(tokenList: List<Token>): List<List<Token>> {
        for (index in tokenList.indices) {
            handleToken(tokenList, index)
        }

        if (singleRow.isNotEmpty()) addRow(singleRow.last())
        if (rows.isEmpty()) throw ParserException("Error: No valid code.")

        return rows
    }

    private fun handleToken(
        tokenList: List<Token>,
        index: Int,
    ) {
        val token = tokenList[index]
        when (token.value) {
            "{" -> openBrace(token)
            "}" -> closeBrace(tokenList, index, token)
            "if" -> startConditional(token)
            "else" -> continueConditional(token)
            ";" -> endStatement(token)
            else -> singleRow.add(token)
        }
    }

    private fun openBrace(token: Token) {
        openBraces++
        singleRow.add(token)
    }

    private fun closeBrace(
        tokenList: List<Token>,
        index: Int,
        token: Token,
    ) {
        openBraces--
        singleRow.add(token)
        if (openBraces != 0) return

        if (index + 1 < tokenList.size && tokenList[index + 1].value == "else") {
            expectingElse = true
        } else {
            addRow(token)
            singleRow = mutableListOf()
            expectingElse = false
        }
    }

    private fun startConditional(token: Token) {
        if (singleRow.isNotEmpty()) addRow(token)
        singleRow.add(token)
        expectingElse = true
    }

    private fun continueConditional(token: Token) {
        if (!expectingElse) return
        singleRow.add(token)
        expectingElse = false
    }

    private fun endStatement(token: Token) {
        if (openBraces != 0 || singleRow.isEmpty()) return
        addRow(token)
        singleRow = mutableListOf()
    }

    private fun addRow(lastToken: Token) {
        if (lastToken.value != ";" && lastToken.value != "}" && lastToken.value != "{") {
            throw ParserException(
                "las sentencias deben finalizar con \";\", \"}\" o \"{\"",
            )
        }
        rows.add(singleRow)
    }
}
