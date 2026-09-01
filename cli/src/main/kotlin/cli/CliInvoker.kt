package cli

import cli.commands.Command
import cli.commands.CommandStatus

class CliInvoker {
    fun runCommand(command: Command): CommandStatus = command.execute()
}
