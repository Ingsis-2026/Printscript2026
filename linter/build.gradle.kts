plugins {
    id("printscript.kotlin-library-conventions")
}

dependencies {
    api(project(":commons"))
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2")

    // El linter analiza un AST que ya viene armado; el lexer y el parser sólo los necesitan
    // los tests, para construirlo a partir de un fuente real.
    testImplementation(project(":lexer"))
    testImplementation(project(":parser"))
}
