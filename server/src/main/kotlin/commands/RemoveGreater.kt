package commands

import ArgumentType
import CommandResult
import data.MusicBand
import utils.auth.token.Content

/**
 * The command removes from the collection all items that exceed the specified.
 *
 * The loop and condition are used to validate the key.
 */
class RemoveGreater : UndoableCommand() {
    override fun getDescription(): String = "remove_greater : удалить из коллекции все элементы, превышающие заданный"

    override fun execute(args: Array<Any>): CommandResult {
        val userElement = args[0] as MusicBand
        val content = args[1] as Content

        //previousPair.clear()
        storage.getCollection { userElement < value }
            .forEach {
                //previousPair.add(it.key to it.value)
                storage.removeKey(content.userId, it.key)
            }
        return CommandResult.Success("Remove_greater")
    }

    override fun undo(): CommandResult {
        throw UnsupportedOperationException("Эта операция не поддерживается в текущей версии")
//        previousPair.forEach { (key, value) ->
//            storage.insert(1, key, value!!)
//        }
//        previousPair.clear()
//        return CommandResult.Success("Undo Remove_greater")
    }

    override fun getArgumentTypes(): Array<ArgumentType> = arrayOf(ArgumentType.MUSIC_BAND)
}
