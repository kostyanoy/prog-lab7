package commands

import ArgumentType
import CommandResult
import data.MusicGenre
import utils.*

/**
 * The command outputs elements whose genre field value is less than the specified one.
 *
 * The condition is used to compare the [genre] field from the collection and the one set by the user.
 */
class FilterLessThanGenre : StorageCommand() {
    override fun getDescription(): String =
        "filter_less_than_genre : вывести элементы, значение поля genre которых меньше заданного"

    override fun execute(args: Array<Any>): CommandResult {
        val userGenre = args[0] as MusicGenre

        val message = buildString {
            storage.getCollection { userGenre > value.genre }.forEach(::appendLine)
        }
        return CommandResult.Success("Filter_less_than_genre", message)
    }

    override fun getArgumentTypes(): Array<ArgumentType> = arrayOf(ArgumentType.GENRE)
}