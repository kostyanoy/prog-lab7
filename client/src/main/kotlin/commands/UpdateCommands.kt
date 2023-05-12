package commands

import ArgumentType
import CommandResult
import Frame
import FrameType
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
        val frame = Frame(FrameType.LIST_OF_COMMANDS_REQUEST)
        frame.setValue("token", interactor.getToken())
        val response = interactor.getClient().sendAndReceiveFrame(frame)

        return when (response.type) {
            FrameType.LIST_OF_COMMANDS_RESPONSE -> update(response)
            FrameType.ERROR -> CommandResult.Failure(
                "Update_commands",
                CommandException("На сервере что-то пошло не так")
            )

            else -> CommandResult.Failure("Update_commands", CommandException("Сервер вернул непотребщину"))
        }
    }

    private fun update(frame: Frame): CommandResult {
        return if (commandManager.updateCommands(frame))
            CommandResult.Success("Update_commands", "Команды обновлены")
        else
            CommandResult.Failure("Update_commands", CommandException("Не удалось обновить команды"))
    }

    override fun getArgumentTypes(): Array<ArgumentType> = arrayOf()
}