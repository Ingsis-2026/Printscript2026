package linter

class LinterOutput {
    var isCorrect: Boolean = true
        private set

    private val brokenRules: MutableList<BrokenRule> = mutableListOf()

    fun addBrokenRule(brokenRule: BrokenRule) {
        isCorrect = false
        brokenRules.add(brokenRule)
    }

    /** Devuelve una copia, para que quien recibe el reporte no pueda modificarlo. */
    fun getBrokenRules(): List<BrokenRule> = brokenRules.toList()
}
