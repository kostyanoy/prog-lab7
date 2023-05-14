package utils

import ArgumentType
import ClientApp
import CommandResult
import Frame
import FrameType
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CommandManagerTest {
    private val client: ClientApp = mockk(relaxed = true)

    @Test
    fun `UpdateCommands return false if wrong frame type`() {
        val frame = Frame(FrameType.COMMAND_REQUEST)
        frame.setValue("commands", mapOf("command" to arrayOf(ArgumentType.INT)))

        val commandManager = CommandManager()

        assertFalse(commandManager.updateCommands(frame))
    }

    @Test
    fun `UpdateCommands return false if has not correct map in body`() {
        val frame = Frame(FrameType.LIST_OF_COMMANDS_RESPONSE)
        frame.setValue("wrong", mapOf("command" to arrayOf(ArgumentType.INT)))

        every { client.sendAndReceiveFrame(any()) } returns frame

        val commandManager = CommandManager()

        assertFalse(commandManager.updateCommands(frame))
    }

    @Test
    fun `UpdateCommands return true if type and body correct`() {
        val frame = Frame(FrameType.LIST_OF_COMMANDS_RESPONSE)
        frame.setValue("commands", mapOf("command" to arrayOf(ArgumentType.INT)))

        every { client.sendAndReceiveFrame(any()) } returns frame

        val commandManager = CommandManager()

        assertTrue(commandManager.updateCommands(frame))
    }

    @Test
    fun `ExecuteCommand return null if got wrong type`() {
        val frame = Frame(FrameType.LIST_OF_COMMANDS_RESPONSE)
        val res = CommandResult.Success("command", "message")
        frame.setValue("data", res)

        every { client.sendAndReceiveFrame(any()) } returns frame

        val commandManager = CommandManager()

        assertEquals(null, commandManager.executeCommand(client, "token", "command", arrayOf(1)))
    }

    @Test
    fun `ExecuteCommand return null if got wrong answer`() {
        val frame = Frame(FrameType.LIST_OF_COMMANDS_RESPONSE)
        frame.setValue("data", "sas")

        every { client.sendAndReceiveFrame(any()) } returns frame

        val commandManager = CommandManager()

        assertEquals(null, commandManager.executeCommand(client, "token", "command", arrayOf(1)))
    }

    @Test
    fun `ExecuteCommand return CommandResult if got the correct answer`() {
        val frame = Frame(FrameType.COMMAND_RESPONSE)
        val res = CommandResult.Success("command", "message")
        frame.setValue("data", res)

        every { client.sendAndReceiveFrame(any()) } returns frame

        val commandManager = CommandManager()

        assertEquals(res, commandManager.executeCommand(client, "token", "command", arrayOf(1)))
    }


}