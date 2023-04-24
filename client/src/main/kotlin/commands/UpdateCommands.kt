package commands

import ArgumentType
import CommandResult
import exceptions.CommandException
import org.koin.core.component.inject
import utils.CommandManager

/**
 * The command receive available server commands
 */
class UpdateCommands : ClientCommand() {
    private val commandManager: CommandManager by inject()
    override fun getDescription(): String = "update_commands : запросить у сервера список доступных команд"

    override fun execute(args: Array<Any>): CommandResult {
        if (commandManager.updateCommands(interactor.getClient())) {
            return CommandResult.Success("Update_commands")
        }
        return CommandResult.Failure("Update_commands", CommandException("Сервер вернул непотребщину"))
    }

    override fun getArgumentTypes(): Array<ArgumentType> = arrayOf()
}