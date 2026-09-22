package interpreter

/**
 * Cómo fue declarada una variable: con qué keyword y con qué tipo.
 *
 * Los dos datos viajan juntos porque los produce la misma sentencia y ninguno significa nada sin
 * el otro: no hay variable declarada sin tipo ni tipo declarado sin keyword. El tipo se recuerda
 * más allá de la declaración porque una asignación posterior lo necesita para resolver un
 * [ExternalInput], cuando el nodo de la declaración ya no está a mano.
 */
class Declaration(
    val keyword: String,
    val declaredType: String,
) {
    /** Una constante no admite reasignación; quien lo verifica es el evaluador de asignaciones. */
    val isConstant: Boolean get() = keyword == "const"
}
