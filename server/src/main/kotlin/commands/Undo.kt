package commands

import ArgumentType
import Command
import CommandResult
import org.koin.core.component.inject

/**
Represents an undo command that undoes the previous executed command.
 */
class Undo : Command() {
    private val history: CommandHistory by inject()
    override fun getDescription(): String = "undo : отмена последней выполненной команды"
    override fun execute(args: Array<Any>): CommandResult = history.undoLastCommand()
    override fun getArgumentTypes(): Array<ArgumentType> = arrayOf()

}