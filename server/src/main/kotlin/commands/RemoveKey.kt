package commands

import ArgumentType
import CommandResult
import exceptions.ParameterException
import utils.auth.token.Content

/**
 * The command removes an item from the collection by its key
 *
 * @exception [ParameterException] used if the element with the specified key does not exist
 */
class RemoveKey : UndoableCommand() {
    override fun getDescription(): String = "remove_key : удалить элемент из коллекции по его ключу"

    override fun execute(args: Array<Any>): CommandResult {
        val userKey = args[0] as Int
        val content = args[1] as Content

        storage.removeKey(content.userId, userKey)
        return CommandResult.Success("Remove_key")
    }

    override fun undo(): CommandResult {
        throw UnsupportedOperationException("Эта операция не поддерживается в текущей версии")
    }

    override fun getArgumentTypes(): Array<ArgumentType> = arrayOf(ArgumentType.INT)
}

