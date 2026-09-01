package rules

import token.Token
import token.TokenType

/**
 * Shared token predicates used by the linter rules.
 */
internal fun Token.isFunctionNamed(name: String): Boolean = getType() == TokenType.FUNCTION && value.lowercase() == name

/**
 * Un token forma parte de una *expresión*, a diferencia de un argumento simple.
 *
 * Son expresión un operador y una llamada anidada. Un identificador o un literal
 * (`NUMBERLITERAL`, `STRINGLITERAL`, `BOOLEANLITERAL`) no lo son: la consigna admite
 * `println(x)` y `println(5)`, y sólo rechaza `println(1 + 2)`.
 */
internal fun Token.isExpressionToken(): Boolean = getType() == TokenType.OPERATOR || getType() == TokenType.FUNCTION
