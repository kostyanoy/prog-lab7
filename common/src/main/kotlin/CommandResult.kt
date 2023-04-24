import CommandResult.Failure
import CommandResult.Success
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

/**
 * Sealed class for results of the commands.
 *
 * Can be either [Success] or [Failure]
 */
sealed class CommandResult {
    /**
     * When command executed successfully
     *
     * @param commandName the name of the executed command
     * @param message text for the user
     */
    @Serializable
    data class Success(val commandName: String, val message: String? = null) : CommandResult()

    /**
     * When command executed wrongly
     *
     * @param commandName the name of the (not) executed command
     * @param throwable the exception which occurred
     */
    @Serializable
    data class Failure(val commandName: String, val throwable: @Contextual Throwable) : CommandResult()
}
