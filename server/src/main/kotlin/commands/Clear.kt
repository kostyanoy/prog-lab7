package commands

import ArgumentType
import CommandResult
import utils.auth.token.Content

/**
 * The command that clears the collection.
 */
class Clear : StorageCommand() {
    override fun getDescription(): String = "clear : очистить коллекцию"

    override fun execute(args: Array<Any>): CommandResult {
        val content = args[0] as Content

        storage.clear(content.userId)
        return CommandResult.Success("Clear")
    }

    override fun getArgumentTypes(): Array<ArgumentType> = arrayOf()
}