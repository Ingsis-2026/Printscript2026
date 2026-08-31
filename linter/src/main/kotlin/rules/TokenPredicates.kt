package rules

import token.Token
import token.TokenType

/**
 * Shared token predicates used by the linter rules.
 */
internal fun Token.isFunctionNamed(name: String): Boolean = getType() == TokenType.FUNCTION && value.lowercase() == name
