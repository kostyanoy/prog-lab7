package commands

import CommandResult
import data.MusicBand

/**

An abstract class that represents an undoable command that operates on a storage of music bands.

It extends the [StorageCommand] class and provides an [undo] method to reverse the effects of the command.
 */
abstract class UndoableCommand : StorageCommand() {
    val previousPair: MutableList<Pair<Int, MusicBand?>> = mutableListOf()
    abstract fun undo(): CommandResult
}