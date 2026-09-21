package interpreter

/**
 * Las variables que el programa creó hasta ahora.
 *
 * Guarda por separado cómo fue declarada cada variable y qué valor tiene, porque una cosa puede
 * darse sin la otra: `let x: number;` declara sin dar valor, y `x = 42` da valor sin declarar.
 * Por eso «¿ya fue declarada?» y «¿ya tiene valor?» son dos preguntas distintas y cada evaluador
 * elige la que le corresponde: la declaración decide si una redeclaración es válida y si una
 * reasignación está permitida; el valor es lo que se lee cuando la variable aparece en una
 * expresión.
 *
 * Las dos colecciones son privadas y se escriben sólo con [declare] y [assign], que no pueden
 * dejarlas en desacuerdo porque no guardan el mismo dato.
 */
class VariableTable {
    private val declarations: MutableMap<String, Declaration> = mutableMapOf()
    private val values: MutableMap<String, Any> = mutableMapOf()

    /** Si el nombre fue declarado, aunque todavía no tenga valor. */
    fun isDeclared(name: String): Boolean = declarations.containsKey(name)

    /** Cómo fue declarado el nombre, o `null` si nunca se declaró. */
    fun declarationOf(name: String): Declaration? = declarations[name]

    /** El valor actual del nombre, o `null` si todavía no tiene ninguno. */
    fun valueOf(name: String): Any? = values[name]

    /** Si el programa todavía no creó ninguna variable. */
    fun isEmpty(): Boolean = declarations.isEmpty() && values.isEmpty()

    /** Registra cómo fue declarado el nombre. El valor, si la declaración trae uno, lo pone [assign]. */
    fun declare(
        name: String,
        declaration: Declaration,
    ) {
        declarations[name] = declaration
    }

    /** Le da valor al nombre. No verifica nada: las reglas de reasignación son del evaluador. */
    fun assign(
        name: String,
        value: Any,
    ) {
        values[name] = value
    }
}
