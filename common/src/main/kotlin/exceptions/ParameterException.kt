package exceptions

/**
 * @exception [ParameterException] used if the parameter does not satisfy the condition
 */
class ParameterException(message: String?) : Throwable(message)