package utils

import ArgumentType
import ClientApp
import CommandResult
import Frame
import FrameType
import commands.ExecuteScript
import commands.Exit
import commands.Help
import commands.UpdateCommands
import exceptions.CommandException

/**
 * Contains available client and server commands.
 */
class CommandManager {
    /**
     * List of all client commands
     */
    private val clientCommands = mapOf(
        "help" to Help(),
        "execute_script" to ExecuteScript(),
        "exit" to Exit(),
        "update_commands" to UpdateCommands(),
    )

    /**
     * List of all commands. Updates with update_commands
     */
    val commands = clientCommands.mapValues { e -> e.value.getArgumentTypes() }.toMutableMap()

    /**
     * Send request to the server to get list of server commands
     *
     * @param response response from the server
     * @return true if the request was successful
     */
    fun updateCommands(response: Frame): Boolean {
        if (response.type != FrameType.LIST_OF_COMMANDS_RESPONSE) return false
        val serverCommands = response.body["commands"] as? Map<String, Array<ArgumentType>> ?: return false
        commands.clear()
        commands.putAll(clientCommands.mapValues { e -> e.value.getArgumentTypes() })
        commands.putAll(serverCommands)
        return true
    }

    /**
     * Checks if the command is from client part
     *
     * @param command checked command
     * @return true if command is from client part
     */
    private fun isClientCommand(command: String): Boolean = command in clientCommands

    /**
     * Execute commands by sending frame to the server
     *
     * @param clientApp the current client connected to the server
     * @return [CommandResult] of the command
     */
    fun executeCommand(clientApp: ClientApp, token: String, command: String, args: Array<Any>): CommandResult? {
        if (isClientCommand(command)) {
            return clientCommands[command]!!.execute(args)
        }
        val frame = Frame(FrameType.COMMAND_REQUEST)
        frame.setValue("token", token)
        frame.setValue("name", command)
        frame.setValue("args", args)
        val respond = clientApp.sendAndReceiveFrame(frame)
        return if (respond.type == FrameType.COMMAND_RESPONSE) respond.body["data"] as? CommandResult else null
    }

    /**
     * Get the arguments needed for command
     *
     * @param command the checked command
     * @return array of [ArgumentType]
     */
    fun getArgs(command: String): Array<ArgumentType> {
        if (command !in commands) {
            throw CommandException("Такой команды не существует")
        }
        return commands[command]!!
    }
}

