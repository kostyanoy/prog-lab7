package commands

import ArgumentType
import CommandResult
import data.MusicBand
import utils.auth.token.Content

/**
 * The command that updates the value of a collection item whose id is equal to the specified one.
 *
 * Fails if the element with the specified key does not exist.
 */
class Update : UndoableCommand() {
    override fun getDescription(): String =
        "update : обновить значение элемента коллекции, id которого равен заданному"

    override fun execute(args: Array<Any>): CommandResult {
        val userKey = args[0] as Int
        val userElement = args[1] as MusicBand
        val content = args[2] as Content

        storage.update(content.userId, userKey, userElement)
        return CommandResult.Success("Update")
    }

    override fun undo(): CommandResult {
        throw UnsupportedOperationException("Эта операция не поддерживается в текущей версии")
    }

    override fun getArgumentTypes(): Array<ArgumentType> = arrayOf(ArgumentType.INT, ArgumentType.MUSIC_BAND)
}
