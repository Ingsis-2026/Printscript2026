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

    override fun format(input: String): String {
        val tokens: List<Token> = lexer.execute(input)
        if (tokens.isEmpty()) return ""

        val tokensWithSemicolon: List<Token> = addSemicolonForEachStatement(tokens)
        val astNodes: List<ASTNode> = parser.execute(tokensWithSemicolon)

        val formatedNodes: List<String> =
            astNodes.map { node ->
                val formatted = formatNode(node)
                // Un condicional ya cierra con "}"; el resto de las sentencias termina en ";".
                if (node is ConditionalNode) formatted else semicolonHandler.handleSemicolon(formatted)
            }

        return lineBreakHandler.handleLineBreak(formatedNodes, 1)
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
     */
    private fun addSemicolonForEachStatement(tokens: List<Token>): List<Token> {
        val lastToken = tokens.lastOrNull() ?: return tokens
        if (lastToken.value == ";" || lastToken.value == "}") return tokens

        return tokens +
            Token(
                TokenType.PUNCTUATOR,
                ";",
                lastToken.getPosition(),
                lastToken.getPosition(),
            )
    }
}
