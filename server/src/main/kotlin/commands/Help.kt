package commands

import ArgumentType
import Command
import CommandResult
import org.koin.core.component.inject
import utils.CommandManager


/**
 * The command displays help for available commands
 */
class Help : Command() {
    private val commandManager: CommandManager by inject()

    override fun getDescription(): String = "help : вывести справку по доступным командам"

    override fun execute(args: Array<Any>): CommandResult {
        val message = buildString {
            commandManager.commands.values.forEach {
                appendLine(it.getDescription())
            }
        }
        return CommandResult.Success("Help", message)
    }

    override fun getArgumentTypes(): Array<ArgumentType> = arrayOf()
}
