package commands

import ArgumentType
import CommandResult

/**
 * The command removes from the collection all items whose key exceeds the specified one.
 *
 * The loop and condition are used to validate the key.
 */
class RemoveGreaterKey : UndoableCommand() {
    override fun getDescription(): String =
        "remove_greater_key : удалить из коллекции все элементы, ключ которых превышает заданный"

    override fun execute(args: Array<Any>): CommandResult {
        previousPair.clear()
        val userKey = args[0] as Int
        storage.getCollection { userKey < key }
            .forEach {
                previousPair.add(it.key to it.value)
                storage.removeKey(it.key)
            }
        return CommandResult.Success("Remove_greater_key")
    }

    override fun getArgumentTypes(): Array<ArgumentType> = arrayOf(ArgumentType.INT)

    override fun undo(): CommandResult {
        previousPair.forEach { (key, value) ->
            storage.insert(key, value!!)
        }
        previousPair.clear()
        return CommandResult.Success("Undo Remove_greater_key")
    }
}


