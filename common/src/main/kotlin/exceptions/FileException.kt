package exceptions

/**
 * @exception [FileException] used if file can't be opened or found
 */
class FileException(message: String?) : Throwable(message)
