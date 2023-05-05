package commands

import ArgumentType
import CommandResult
import data.MusicBand
import utils.auth.token.Content

/**
 * The command replaces the value by the key if the new value is less than the old one.
 *
 * The condition is used to check the value by the key.
 * Fails if the element with the specified key does not exist.
 */
class ReplaceIfLowe : StorageCommand() {
    override fun getDescription(): String =
        "replace_if_lowe : заменить значение по ключу, если новое значение меньше старого"

    override fun execute(args: Array<Any>): CommandResult {
        val userKey = args[0] as Int
        val userElement = args[1] as MusicBand
        val content = args[2] as Content

        storage.update(content.userId, userKey, userElement)
        return CommandResult.Success("Replace_if_lowe")
    }

    override fun getArgumentTypes(): Array<ArgumentType> = arrayOf(ArgumentType.INT, ArgumentType.MUSIC_BAND)
}


