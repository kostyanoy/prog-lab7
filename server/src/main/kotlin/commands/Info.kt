package commands

import ArgumentType
import CommandResult

/**
 * The command outputs information about the collection to the standard output stream
 */
class Info : StorageCommand() {
    override fun getDescription(): String = "info : вывести в стандартный поток вывода информацию о коллекции"

    override fun execute(args: Array<Any>): CommandResult {
        return CommandResult.Success("Info", storage.getInfo())
    }

    override fun getArgumentTypes(): Array<ArgumentType> = arrayOf()
}
