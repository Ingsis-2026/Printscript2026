package formatter

import ast.ASTNode
import ast.ConditionalNode
import formatoperations.FormattingOperation
import formatoperations.commons.LineBreakHandler
import formatoperations.commons.SemicolonHandler
import lexer.Lexer
import parser.Parser
import rules.RulesReader
import token.Token
import token.TokenType

class FormatterPS(
    private val rulesReader: RulesReader,
    private val rulesPath: String,
    private val formattingOperations: List<FormattingOperation>,
    private val lexer: Lexer,
    private val parser: Parser,
) : Formatter {
    private val semicolonHandler = SemicolonHandler()
    private val lineBreakHandler = LineBreakHandler()

    /**
     * Formatea un fuente sentencia por sentencia, de forma perezosa.
     *
     * Es el punto de entrada para archivos grandes: el consumidor puede escribir cada
     * sentencia formateada a medida que la recibe, sin armar el resultado completo en memoria.
     */
    fun formatStatements(lines: Sequence<String>): Sequence<String> =
        parser
            .execute(withTrailingSemicolon(lexer.convertToTokens(lines)))
            .map { node ->
                val formatted = formatNode(node)
                // Un condicional ya cierra con "}"; el resto de las sentencias termina en ";".
                if (node is ConditionalNode) formatted else semicolonHandler.handleSemicolon(formatted)
            }

    override fun format(input: String): String {
        if (input.isBlank()) return ""
        val formatted = formatStatements(input.lineSequence()).toList()
        return lineBreakHandler.handleLineBreak(formatted, 1)
    }

    override fun format(astNode: ASTNode): String = formatNode(astNode)

    private fun formatNode(node: ASTNode): String {
        val formatter =
            formattingOperations.find { it.canHandle(node) }
                ?: error("No FormattingOperation registered for ${node::class.simpleName}")
        return formatter.format(node, this)
    }

    /** Las reglas se leen y validan una sola vez: el archivo no cambia durante el formateo. */
    private val cachedRules: Map<String, Any> by lazy { rulesReader.readFile(rulesPath) }

    override fun getRules(): Map<String, Any> = cachedRules

    /**
     * El Lexer descarta los saltos de línea, así que la última sentencia puede quedar sin
     * terminador. Se agrega un ";" al final sólo si la entrada no cierra ya con ";" o "}".
     *
     * Retiene un único token (el anterior), así que sigue sirviendo para un flujo que no
     * cabe en memoria.
     */
    private fun withTrailingSemicolon(tokens: Sequence<Token>): Sequence<Token> =
        sequence {
            var lastToken: Token? = null
            for (token in tokens) {
                yield(token)
                lastToken = token
            }

            val previous = lastToken ?: return@sequence
            if (previous.value != ";" && previous.value != "}") {
                yield(
                    Token(
                        TokenType.PUNCTUATOR,
                        ";",
                        previous.getFinalPosition(),
                        previous.getFinalPosition(),
                    ),
                )
            }
        }
}
