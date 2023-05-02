package commands

import ArgumentType
import CommandResult
import data.MusicBand
import utils.auth.token.Content

/**
 * The command adds a new element with the specified key.
 *
 * Fails if the element with the specified key already exist.
 */
class Insert : UndoableCommand() {
    override fun getDescription(): String = "insert : добавить новый элемент с заданным ключом"

    override fun execute(args: Array<Any>): CommandResult {
        val userKey = args[0] as Int
        val userElement = args[1] as MusicBand
        val content = args[2] as Content

        //previousPair.clear()
        //val collection = storage.getCollection { key == userKey }
//        if (userKey in collection.keys) {
//            return CommandResult.Failure("Insert", ParameterException("Элемент с таким ключом уже существует"))
//        }
        //val pair = (userKey to collection[userKey])
        storage.insert(content.userId, userKey, userElement)
        //previousPair.add(pair)
        return CommandResult.Success("Insert")
    }

    override fun undo(): CommandResult {
        throw UnsupportedOperationException("Эта операция не поддерживается в текущей версии")
//        previousPair.forEach { (key) ->
//            storage.removeKey(1, key)
//        }
//        previousPair.clear()
//        return CommandResult.Success("Undo Insert")
    }

    override fun getArgumentTypes(): Array<ArgumentType> = arrayOf(ArgumentType.INT, ArgumentType.MUSIC_BAND)
}