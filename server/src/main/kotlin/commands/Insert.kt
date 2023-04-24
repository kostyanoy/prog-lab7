package commands

import ArgumentType
import CommandResult
import data.MusicBand
import exceptions.ParameterException

/**
 * The command adds a new element with the specified key.
 *
 * Fails if the element with the specified key already exist.
 */
class Insert : UndoableCommand() {
    override fun getDescription(): String = "insert : добавить новый элемент с заданным ключом"

    override fun execute(args: Array<Any>): CommandResult {
        previousPair.clear()
        val userKey = args[0] as Int
        val collection = storage.getCollection { true }
        if (userKey in collection.keys) {
            return CommandResult.Failure("Insert", ParameterException("Элемент с таким ключом уже существует"))
        }
        previousPair.add(userKey to collection[userKey])
        storage.insert(userKey, args[1] as MusicBand)
        return CommandResult.Success("Insert")
    }

    override fun undo(): CommandResult {
        previousPair.forEach { (key) ->
            storage.removeKey(key)
        }
        previousPair.clear()
        return CommandResult.Success("Undo Insert")
    }

    override fun getArgumentTypes(): Array<ArgumentType> = arrayOf(ArgumentType.INT, ArgumentType.MUSIC_BAND)
}