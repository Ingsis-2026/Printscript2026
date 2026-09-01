package formatoperations

import ast.ASTNode
import ast.DeclarationNode
import ast.NilNode
import formatoperations.commons.SpaceHandler
import formatter.Formatter

class DeclarationFormatter(
    private val allowedDeclarationKeywords: List<String>,
    private val allowedDataTypes: List<String>,
) : FormattingOperation {
    private val spaceHandler: SpaceHandler = SpaceHandler()

    override fun canHandle(astNode: ASTNode): Boolean = astNode is DeclarationNode

    /**
     * Arma una declaración con la forma `let x : number = 5`, o `let x : number` cuando no
     * tiene inicializador. El `;` final lo agrega quien invoca al formatter.
     *
     * Ojo con los separadores: [SpaceHandler] los devuelve **con los espacios ya
     * incorporados** según las reglas configuradas, así que `colonSeparator(...)` puede valer
     * `" : "`, `": "`, `" :"` o `":"`. Por eso las plantillas de abajo no llevan espacios
     * propios alrededor de ellos; el único espacio literal es el que separa la palabra clave
     * del identificador, que no es configurable.
     */
    override fun format(
        node: ASTNode,
        formatter: Formatter,
    ): String {
        if (!canHandle(node)) error("Node isn't a DeclarationNode")
        val declarationNode = node as DeclarationNode

        val keyword = declarationKeywordOrFail(declarationNode.declValue)
        val dataType = dataTypeOrFail(declarationNode.dataTypeValue)

        // let x : number
        val declaration = "$keyword ${declarationNode.id}${colonSeparator(formatter)}$dataType"

        // Sin inicializador la declaración termina acá.
        if (declarationNode.expr is NilNode) return declaration

        // Se delega en el Formatter, que despacha sobre todas las operaciones registradas:
        // el inicializador puede ser un literal, una operación o una llamada a función.
        val initializer = formatter.format(declarationNode.expr)

        // let x : number = 5
        return "$declaration${equalsSeparator(formatter)}$initializer"
    }

    /** `:` con los espacios que indiquen `spaceBeforeColon` y `spaceAfterColon`. */
    private fun colonSeparator(formatter: Formatter): String =
        spaceHandler.handleSpace(
            ":",
            formatter.getRules()["spaceBeforeColon"] as Boolean,
            formatter.getRules()["spaceAfterColon"] as Boolean,
        )

    /** `=` con los espacios que indique `spaceAroundEquals`, a ambos lados. */
    private fun equalsSeparator(formatter: Formatter): String {
        val spaceAroundEquals = formatter.getRules()["spaceAroundEquals"] as Boolean
        return spaceHandler.handleSpace("=", spaceAroundEquals, spaceAroundEquals)
    }

    private fun declarationKeywordOrFail(declKeyword: String): String {
        if (declKeyword !in allowedDeclarationKeywords) {
            throw UnsupportedOperationException("Unsupported declaration type $declKeyword")
        }
        return declKeyword
    }

    private fun dataTypeOrFail(dataType: String): String {
        if (dataType !in allowedDataTypes) {
            throw UnsupportedOperationException("Unsupported data type $dataType")
        }
        return dataType
    }
}
