package exceptions

/**
 * @exception [CommandException] used if the command is not detected
 */
class CommandException(message: String?) : Throwable(message)
