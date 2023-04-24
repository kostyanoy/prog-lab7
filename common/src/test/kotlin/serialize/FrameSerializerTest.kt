package serialize

import ArgumentType
import CommandResult
import Frame
import FrameType
import data.Coordinates
import data.MusicBand
import data.MusicGenre
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertContentEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FrameSerializerTest {
    private val serializer = FrameSerializer()

    @Test
    fun `Command failure must have same commands name and message`() {
        val frame = Frame(FrameType.COMMAND_RESPONSE)
        val throwable = Throwable("message")
        frame.setValue("result", CommandResult.Failure("command", throwable))

        val s = serializer.serialize(frame)
        val d = serializer.deserialize(s)

        val res = d.body["result"] as CommandResult.Failure

        assertEquals("command", res.commandName)
        assertEquals("message", res.throwable.message)
    }

    @Test
    fun `Command success must have same commands name and message`() {
        val frame = Frame(FrameType.COMMAND_RESPONSE)
        frame.setValue("result", CommandResult.Success("command", "message"))

        val s = serializer.serialize(frame)
        val d = serializer.deserialize(s)

        val res = d.body["result"] as CommandResult.Success

        assertEquals("command", res.commandName)
        assertEquals("message", res.message)
    }

    @Test
    fun `Map of the commands should be correct`() {
        val frame = Frame(FrameType.LIST_OF_COMMANDS_RESPONSE)
        frame.setValue(
            "commands", mapOf(
                "name1" to arrayOf(ArgumentType.INT, ArgumentType.STRING),
                "name2" to arrayOf(ArgumentType.GENRE),
                "name3" to arrayOf()
            )
        )

        val s = serializer.serialize(frame)
        val d = serializer.deserialize(s)

        val map = d.body["commands"] as Map<String, Array<ArgumentType>>

        assertContentEquals(arrayOf(ArgumentType.INT, ArgumentType.STRING), map["name1"])
        assertContentEquals(arrayOf(ArgumentType.GENRE), map["name2"])
        assertContentEquals(arrayOf(), map["name3"])
    }

    @Test
    fun `Array of the args should be correct`() {
        val band = MusicBand("1", Coordinates(1.0F, 1.0), 1, 1, "d", MusicGenre.SOUL, null)
        val frame = Frame(FrameType.COMMAND_REQUEST)
        frame.setValue("name", "command")
        frame.setValue("args", arrayOf(1, "arg", MusicGenre.HIP_HOP, band))

        val s = serializer.serialize(frame)
        val d = serializer.deserialize(s)

        val array = d.body["args"] as Array<Any>

        assertEquals("command", d.body["name"] as String)
        assertEquals(1, array[0] as Int)
        assertEquals("arg", array[1] as String)
        assertEquals(MusicGenre.HIP_HOP, array[2] as MusicGenre)
        assertEquals(band, array[3] as MusicBand)
    }
}
