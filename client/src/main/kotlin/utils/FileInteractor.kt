package utils

import ArgumentType
import ClientApp
import data.Album
import data.Coordinates
import data.MusicBand
import data.MusicGenre
import exceptions.CommandFileException
import org.koin.core.component.KoinComponent

/**
 * Executes commands from the file
 *
 * @param interactor delegates to this most of the methods
 * @param lines have all lines from the command file
 * @throws [CommandFileException] if exception occurred
 */
class FileInteractor(
    private val interactor: Interactor,
    private val lines: List<String>
) : KoinComponent, Interactor by interactor, Validator {
    private var index = 0
    private var lastArgument: String? = null
    private lateinit var clientApp: ClientApp

    /**
     * Starts executing commands from file
     *
     * @param clientApp the current client connected to the server
     */
    override fun start(clientApp: ClientApp) {
        this.clientApp = clientApp
        while (hasNext()) {
            interact(next())
        }
    }

    /**
     * Stops executing file commands
     */
    override fun exit() {
        interactor.exit()
        index = lines.count()
    }

    /**
     * Takes commands one by one and arguments in the line. Tries to execute command
     *
     * @param text the string with the command
     */
    override fun interact(text: String) {
        val input = text.split(" ")
        if (input.count() > 2) {
            throw CommandFileException("Слишком много аргументов в строке")
        }
        try {
            val command = input[0]
            lastArgument = if (input.count() == 2) input[1] else null
            executeCommand(command)
        } catch (e: CommandFileException) {
            throw e
        } catch (e: Throwable) {
            throw CommandFileException(e.message)
        }
    }

    /**
     * Gets arguments from files by checking last argument or next line
     *
     * @param argTypes array with [ArgumentType]
     * @return Any array filled with needed arguments
     */
    override fun getArgs(argTypes: Array<ArgumentType>): Array<Any> {
        val args = arrayListOf<Any>()
        argTypes.forEach {
            args.add(
                when (it) {
                    ArgumentType.INT -> getInt()
                    ArgumentType.STRING -> getString()
                    ArgumentType.GENRE -> getGenre()
                    ArgumentType.MUSIC_BAND -> getMusicBand()
                }
            )
        }
        return args.toArray()
    }

    override fun getString(): String = lastArgument ?: throw CommandFileException("Нет аргумента")
    override fun getInt(): Int = lastArgument?.toIntOrNull() ?: throw CommandFileException("Не Int")
    override fun getGenre(): MusicGenre =
        lastArgument?.let { MusicGenre.valueOfOrNull(it) } ?: throw CommandFileException("Не MusicGenre")

    override fun getMusicBand(): MusicBand {
        val name = next()
        val coordinates = Coordinates(
            next().toFloatOrNull() ?: throw CommandFileException("Не Float"),
            next().toDoubleOrNull() ?: throw CommandFileException("Не Double")
        )
        val numberOfParticipants = next().toIntOrNull() ?: throw CommandFileException("Не Int")
        val albumsCount = next().toLongOrNull()
        val description = next()
        val genre = MusicGenre.valueOfOrNull(next()) ?: throw CommandFileException("Не MusicGenre`")
        var album: Album? = null
        val albumName = next()
        if (albumName.isNotEmpty()) {
            val albumLength = next().toLongOrNull() ?: throw CommandFileException("Не Long")
            album = Album(albumName, albumLength)
        }
        return MusicBand(
            name = name,
            coordinates = coordinates,
            numberOfParticipants = numberOfParticipants,
            albumsCount = albumsCount,
            description = description,
            genre = genre,
            bestAlbum = album
        )
    }

    /**
     * @return next line of the command file
     */
    fun next(): String {
        if (hasNext()) return lines[index++]
        throw CommandFileException("Недостаточно строк")
    }

    /**
     * Check if there is next line of the command file
     *
     * @return true if it is not last line yet
     */
    fun hasNext(): Boolean = (index < lines.count())
}