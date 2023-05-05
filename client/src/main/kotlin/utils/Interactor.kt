package utils

import ArgumentType
import ClientApp
import utils.state.InteractionState

/**
 * Interface for connecting parts of the program
 */
interface Interactor {
    /**
     * Starts the interaction with the user
     */
    fun start(clientApp: ClientApp)

    /**
     * Ends interaction with user
     */
    fun exit()

    /**
     * Handles the string with command and possible argument
     *
     * @param text line from the user with the name of the command
     */
    fun interact(text: String)

    /**
     * @param message to the user
     */
    fun showMessage(message: String)

    /**
     * Executes the command file located at [path] in the OS
     *
     * @param path the location of the file in the OS
     */
    fun executeCommandFile(path: String)

    /**
     * Get arguments from user
     *
     * @param argTypes the list of [ArgumentType]
     */
    fun getArgs(argTypes: Array<ArgumentType>): Array<Any>

    /**
     * Executes the command aka sending request to server
     *
     * @param command the name of the command
     */
    fun executeCommand(command: String)

    /**
     * Returns current client connected to the server
     *
     * @return the client connected to the server
     */
    fun getClient(): ClientApp

    /**
     * @param token string-token from the server
     */
    fun setToken(token: String)

    /**
     * @return token string-token from the server
     */
    fun getToken(): String

    /**
     * @param state next interaction state
     */
    fun setState(state: InteractionState)
}
