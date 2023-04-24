package commands

import Command
import CommandResult
import exceptions.CommandException

/**
 * Class that stores the history of executed commands.
 */

class CommandHistory {
    /**
     * List of executed commands.
     */
    private val history = mutableListOf<UndoableCommand>()

    /**
     * Add executed command to history.
     * @param [command] Executed command.
     */
    fun executedCommand(command: Command) {
        if (command !is UndoableCommand) {
            return
        }
        history.add(command)
    }

    /**
     * Undo the last executed command.
     */
    fun undoLastCommand(): CommandResult {
        if (history.isNotEmpty()) {
            val last = history.removeLast()
            return last.undo()
        }
        return CommandResult.Failure("Undo", CommandException("Нечего отменять"))
    }
}