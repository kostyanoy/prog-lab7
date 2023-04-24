package commands

import ArgumentType
import CommandResult
import data.MusicBand
import exceptions.ParameterException

/**
 * The command that updates the value of a collection item whose id is equal to the specified one.
 *
 * Fails if the element with the specified key does not exist.
 */
class Update : UndoableCommand() {
    override fun getDescription(): String =
        "update : обновить значение элемента коллекции, id которого равен заданному"

    override fun execute(args: Array<Any>): CommandResult {
        previousPair.clear()
        val userKey = args[0] as Int
        val collection = storage.getCollection { true }
        if (userKey !in collection.keys) {
            return CommandResult.Failure("Update", ParameterException("Элемента с таким ключом не существует"))
        }
        previousPair.add(userKey to collection[userKey])
        storage.update(userKey, args[1] as MusicBand)
        return CommandResult.Success("Update")
    }

    override fun undo(): CommandResult {
        previousPair.forEach { (key, value) ->
            storage.update(key, value!!)
        }
        previousPair.clear()
        return CommandResult.Success("Undo Update")
    }

    override fun getArgumentTypes(): Array<ArgumentType> = arrayOf(ArgumentType.INT, ArgumentType.MUSIC_BAND)
}
