package utils

import data.Album
import data.Coordinates
import data.MusicBand
import data.MusicGenre

/**
 * Implements [Validator] interface for getting values from user. Repeats the input process until user give correct value
 *
 * @param interactor used to show messages to user
 * @param userManager used to get input from user and show messages
 */
class ValidationManager(
    private val interactor: Interactor,
    private val userManager: ReaderWriter
) : Validator {

    override fun getInt(): Int {
        var res: Int? = null
        while (res == null) {
            userManager.write("Вы должны ввести аргумент типа число: ")
            res = userManager.readLine().toIntOrNull()
        }
        return res
    }

    override fun getString(): String {
        userManager.write("Вы должны ввести аргумент типа строка: ")
        return userManager.readLine()
    }

    override fun getGenre(): MusicGenre {
        var res: MusicGenre? = null
        while (res == null) {
            userManager.write("Вы должны ввести жанр (PROGRESSIVE_ROCK, HIP_HOP, PSYCHEDELIC_CLOUD_RAP, SOUL, POST_PUNK): ")
            res = MusicGenre.valueOfOrNull(userManager.readLine().uppercase())
        }
        return res
    }

    override fun getMusicBand(): MusicBand {
        val name = userManager.getValidatedValue("Введите название банды: ") {
            it.isNotEmpty()
        }
        val coordX = userManager.getValidatedValue("Введите координату Х (<=552): ") {
            (it.toFloatOrNull() ?: return@getValidatedValue false) <= 552
        }.toFloat()
        val coordY = userManager.getValidatedValue("Введите координату Y: ") {
            it.toDoubleOrNull() != null
        }.toDouble()
        val participants = userManager.getValidatedValue("Введите количество участников: ") {
            (it.toIntOrNull() ?: return@getValidatedValue false) > 0
        }.toInt()
        val albumCount = userManager.getValidatedValue("Введите количество альбомов (может быть пустым): ") {
            it.isEmpty() || (it.toIntOrNull() ?: return@getValidatedValue false) > 0
        }.toLongOrNull()
        val description = userManager.getValidatedValue("Введите описание банды: ") { true }
        val genre =
            userManager.getValidatedValue("Введите жанр (PROGRESSIVE_ROCK, HIP_HOP, PSYCHEDELIC_CLOUD_RAP, SOUL, POST_PUNK): ") {
                MusicGenre.valueOfOrNull(it.uppercase()) != null
            }
        val albumName = userManager.getValidatedValue("Введите название лучшего альбома (может быть пустым): ") { true }
        var album: Album? = null
        if (albumName.isNotEmpty()) {
            val albumLength = userManager.getValidatedValue("Введите длительность альбома: ") {
                (it.toLongOrNull() ?: return@getValidatedValue false) > 0
            }
            album = Album(albumName, albumLength.toLong())
        }

        return MusicBand(
            name = name,
            coordinates = Coordinates(coordX, coordY),
            numberOfParticipants = participants,
            albumsCount = albumCount,
            description = description,
            genre = MusicGenre.valueOf(genre.uppercase()),
            bestAlbum = album
        )
    }
}