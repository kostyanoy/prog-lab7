package commands

import ArgumentType
import CommandResult
import utils.*

/**
 * The command outputs the number of elements whose description field value is greater than the specified one.
 *
 * @property [countDescription] counts the number of elements of the [MusicBand.description] field.
 * The condition is used to compare the [description] field from the collection and the one set by the user.
 */
class CountGreaterThanDescription : StorageCommand() {
    override fun getDescription(): String =
        "count_greater_than_description : вывести количество элементов, значение поля description которых больше заданного"

    override fun execute(args: Array<Any>): CommandResult {
        val userDescription = args[0] as String

        val countDescription = storage.getCollection { userDescription < value.description }
            .count()
        return CommandResult.Success("Count_greater_than_description", "$countDescription")
    }

    override fun getArgumentTypes(): Array<ArgumentType> = arrayOf(ArgumentType.STRING)
}

