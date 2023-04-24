package commands

import ArgumentType
import CommandResult
import org.koin.core.component.inject
import utils.CommandManager

/**
 * The command displays available commands
 */
class Help : ClientCommand() {
    private val commandManager: CommandManager by inject()

    override fun getDescription(): String = "help : вывести доступные команды"

    override fun execute(args: Array<Any>): CommandResult {
        val message = buildString {
            commandManager.commands.forEach {
                appendLine(it.key)
            }
        }
        return CommandResult.Success("Help", message.trim())
    }

    override fun getArgumentTypes(): Array<ArgumentType> = arrayOf()
}