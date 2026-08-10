package token

enum class TokenType {
    IDENTIFIER, // Identificadores como nombres de variables o funciones
    KEYWORD, // Palabras clave como let, const
    PUNCTUATOR, // Símbolos de puntuación () , ; . (1.1 {})
    OPERATOR, // Operadores matemáticos y lógicos como +, -, *, /
    STRINGLITERAL, // Literales de cadena como "hello"
    NUMBERLITERAL, // Literales numéricos como 123, 45.67
    BOOLEANLITERAL, // Literales booleanos como true, false
    DECLARATOR, // Declaradores como :
    LITERAL, // Literal generica.

    ASSIGNATION, // Operador de asignación =
    DATA_TYPE, // Tipos de datos como number, string
    FUNCTION, // Funciones predefinidas como println readInput readEnv
    CONDITIONAL, // Palabras clave condicionales como if, else
    UNKNOWN // Tokens no reconocidos
}
