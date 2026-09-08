package formatter

import lexer.Lexer
import lexer.TokenMapper
import rules.FormattingRules
import rules.RulesReader
import java.io.InputStream

open class FormatterBuilderPS : FormatterBuilder {
    override fun build(
        rulesPath: String,
        version: String,
    ): Formatter = build(RulesReader().readFile(rulesPath), version)

    /** Variante para quien ya tiene la configuración en un flujo y no en un archivo. */
    fun build(
        config: InputStream,
        version: String,
    ): Formatter = build(RulesReader().read(config), version)

    fun build(
        rules: FormattingRules,
        version: String,
    ): Formatter = TokenFormatter(rules, Lexer(TokenMapper(version)))
}
