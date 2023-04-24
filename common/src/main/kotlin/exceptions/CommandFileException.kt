package exceptions

/**
 * @exception [CommandFileException] used in case of unsatisfactory content of the command file
 */
class CommandFileException(message: String?) : Throwable("Ошибка в файле команд: $message")