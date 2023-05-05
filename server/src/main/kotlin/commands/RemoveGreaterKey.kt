package commands

import ArgumentType
import CommandResult
import utils.auth.token.Content

/**
 * The command removes from the collection all items whose key exceeds the specified one.
 *
 * The loop and condition are used to validate the key.
 */
class RemoveGreaterKey : UndoableCommand() {
    override fun getDescription(): String =
        "remove_greater_key : удалить из коллекции все элементы, ключ которых превышает заданный"

    override fun execute(args: Array<Any>): CommandResult {
        val userKey = args[0] as Int
        val content = args[1] as Content

        storage.getCollection { userKey < key }
            .forEach {
                storage.removeKey(content.userId, it.key)
            }
        return CommandResult.Success("Remove_greater_key")
    }

    override fun getArgumentTypes(): Array<ArgumentType> = arrayOf(ArgumentType.INT)

    override fun undo(): CommandResult {
        throw UnsupportedOperationException("Эта операция не поддерживается в текущей версии")
    }
}


