package commands

import ArgumentType
import CommandResult
import exceptions.FileException
import org.jetbrains.kotlin.konan.file.File

/**
 * The command reads and executes the script from the specified file.
 *
 * Fails if no file is found.
 */
class ExecuteScript : ClientCommand() {
    override fun getDescription(): String =
        "execute_script : считать и исполнить скрипт из указанного файла. В скрипте содержатся команды в таком же виде, в котором их вводит пользователь в интерактивном режиме."

    override fun execute(args: Array<Any>): CommandResult {
        val path = args[0] as String
        if (!File(path).exists) {
            return CommandResult.Failure("Execute_script", FileException("Файла команд не обнаружено"))
        }
        try {
            interactor.executeCommandFile(path)
        } catch (e: Throwable) {
            return CommandResult.Failure("Execute_script", e)
        }
        return CommandResult.Success("Execute_script")
    }

    override fun getArgumentTypes(): Array<ArgumentType> = arrayOf(ArgumentType.STRING)
}