package parser

import token.Token
import token.TokenType

/*
 * El papel de un token en la estructura del programa se decide por su tipo y no sólo por su
 * texto: un string literal llega sin comillas, así que el de `println("}")` vale `}` igual que
 * una llave, y en 1.0 `if` es un nombre de variable como cualquier otro.
 */

internal val Token.opensBlock: Boolean get() = isPunctuator("{")

internal val Token.closesBlock: Boolean get() = isPunctuator("}")

internal val Token.endsStatement: Boolean get() = isPunctuator(";")

internal val Token.opensParenthesis: Boolean get() = getType() == TokenType.PARENTHESIS && value == "("

internal val Token.closesParenthesis: Boolean get() = getType() == TokenType.PARENTHESIS && value == ")"

internal val Token.startsConditional: Boolean get() = isConditional("if")

internal val Token.continuesConditional: Boolean get() = isConditional("else")

internal fun Token.isOperator(symbol: String): Boolean = getType() == TokenType.OPERATOR && value == symbol

private fun Token.isPunctuator(symbol: String): Boolean = getType() == TokenType.PUNCTUATOR && value == symbol

private fun Token.isConditional(word: String): Boolean = getType() == TokenType.CONDITIONAL && value == word
