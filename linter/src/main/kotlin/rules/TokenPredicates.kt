package rules

import token.Token
import token.TokenType

/**
 * Shared token predicates used by the linter rules.
 */
internal fun Token.isFunctionNamed(name: String): Boolean = getType() == TokenType.FUNCTION && value.lowercase() == name

/**
 * Un token forma parte de una *expresión* (a diferencia de un argumento simple).
 *
 * Se identifica por lo que sí es una expresión —un operador, o una llamada anidada— en lugar
 * de enumerar lo que no lo es: la lista negra anterior sólo excluía `TokenType.LITERAL`, un
 * valor que ni el Lexer ni el Tokenizer producen, por lo que los literales reales
 * (`NUMBERLITERAL`, `STRINGLITERAL`, `BOOLEANLITERAL`) contaban como expresión.
 */
internal fun Token.isExpressionToken(): Boolean = getType() == TokenType.OPERATOR || getType() == TokenType.FUNCTION
