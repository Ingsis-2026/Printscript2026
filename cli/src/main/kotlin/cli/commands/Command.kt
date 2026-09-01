package cli.commands

interface Command {
    fun execute(): CommandStatus
}
