package cli

import cli.io.ConsoleOutput
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    exitProcess(PrintScriptCli(ConsoleOutput()).run(args.toList()))
}
