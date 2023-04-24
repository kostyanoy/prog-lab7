import org.koin.core.component.KoinComponent


/**
 * An abstract class for defining commands.
 */
abstract class Command : KoinComponent {

    /**
     * Starts the execution of the command
     *
     * @return [CommandResult] with the name of the command and data or exception returned by the command
     */
    abstract fun execute(args: Array<Any>): CommandResult

    /**
     * @return a description of the command.
     */
    abstract fun getDescription(): String

    /**
     * @return array of [ArgumentType] for the command
     */
    abstract fun getArgumentTypes(): Array<ArgumentType>
}
