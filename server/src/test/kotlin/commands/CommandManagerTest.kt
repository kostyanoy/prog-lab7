package commands

import exceptions.CommandException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import utils.CommandManager

class CommandManagerTest {
    @Test
    fun `Get existing command`() {
        val commandManager = CommandManager()

        val command = "info"
        val expectedCommand = Info()

        val realCommand = commandManager.getCommand(command)

        assertEquals(expectedCommand.javaClass, realCommand.javaClass)
    }

    @Test
    fun `CommandManager throws CommandException if command does not exist `() {
        val commandManager = CommandManager()

        val command = "unknown"

        assertThrows<CommandException> { commandManager.getCommand(command) }
    }
}