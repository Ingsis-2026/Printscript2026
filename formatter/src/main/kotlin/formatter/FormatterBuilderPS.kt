package formatter

import lexer.Lexer
import lexer.TokenMapper
import rules.FormattingRules
import rules.RulesReader
import version.Version
import java.io.InputStream

open class FormatterBuilderPS : FormatterBuilder {
    override fun build(
        rulesPath: String,
        version: Version,
    ): Formatter = build(RulesReader().readFile(rulesPath), version)

    /** Para el TCK, que tiene la configuración en un flujo y la versión como texto. */
    fun build(
        config: InputStream,
        version: String,
    ): Formatter = build(RulesReader().read(config), Version.parse(version))

    fun build(
        rules: FormattingRules,
        version: Version,
    ): Formatter = TokenFormatter(rules, Lexer(TokenMapper(version)))
}
