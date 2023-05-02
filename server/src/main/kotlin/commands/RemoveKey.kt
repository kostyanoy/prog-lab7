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

//        previousPair.clear()
//        val collection = storage.getCollection { true }
//        if (userKey !in collection.keys) {
//            return CommandResult.Failure("Remove_greater", ParameterException("Элемента с таким ключом не существует"))
//        }
//        previousPair.add(userKey to collection[userKey])
        storage.removeKey(content.userId, userKey)
        return CommandResult.Success("Remove_key")
    }

    override fun undo(): CommandResult {
        throw UnsupportedOperationException("Эта операция не поддерживается в текущей версии")
//        previousPair.forEach { (key, value) ->
//            storage.insert(1, key, value!!)
//        }
//        previousPair.clear()
//        return CommandResult.Success("Undo Remove_key")
    }

    override fun getArgumentTypes(): Array<ArgumentType> = arrayOf(ArgumentType.INT)
}

