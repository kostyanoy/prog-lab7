package utils

/**
 * Interface for showing messages for user and gaining input data
 */
interface ReaderWriter {
    /**
     * Reads new line from user
     *
     * @return [String] input of user
     */
    fun readLine(): String

    /**
     * Reads a line but doesn't show characters in console
     */
    fun readPassword(): String

    /**
     * Shows the message to user. Adds a new line to the end
     *
     * @param text the content of the message
     */
    fun writeLine(text: String)

    /**
     * Shows the message to user. Does not add a new line to the end
     *
     * @param text the content of the message
     */
    fun write(text: String)

    /**
     * Repeats the input process until it satisfy the validator
     *
     * @param message shown one time before the input
     * @param validator is the condition that input must satisfy
     */
    fun getValidatedValue(message: String, validator: (String) -> Boolean): String
}