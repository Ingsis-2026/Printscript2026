package linter

class LinterOutput {
    var isCorrect: Boolean = true
        private set

    private val brokenRules: MutableList<BrokenRule> = mutableListOf()

    /** Representación textual de cada violación, derivada de la lista tipada. */
    val brokenRulesList: List<String>
        get() = brokenRules.map { formatBrokenRule(it) }

    fun addBrokenRule(brokenRule: BrokenRule) {
        isCorrect = false
        brokenRules.add(brokenRule)
    }

    private fun formatBrokenRule(brokenRule: BrokenRule): String {
        val position = brokenRule.errorPosition
        return "Broken rule: ${brokenRule.ruleDescription} at ${position.row}:${position.column}"
    }

    /**
     * Las violaciones se conservan tipadas: antes se formateaban a String y se volvían a
     * parsear partiendo por " at " y ":", lo que corrompía la posición si la descripción
     * contenía esas subcadenas.
     */
    fun getBrokenRules(): List<BrokenRule> = brokenRules.toList()
}
