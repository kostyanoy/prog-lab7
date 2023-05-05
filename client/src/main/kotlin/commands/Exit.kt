package commands

import ArgumentType
import CommandResult

/**
 * The command terminates the connection to the server.
 */
class Exit : ClientCommand() {
    override fun getDescription(): String = "exit : закрыть соединение с сервером"

    override fun execute(args: Array<Any>): CommandResult {
//        interactor.exit()
        return CommandResult.Success("Exit")
    }

    override fun getArgumentTypes(): Array<ArgumentType> = arrayOf()
}