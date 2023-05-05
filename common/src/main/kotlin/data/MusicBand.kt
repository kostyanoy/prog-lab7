package data

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.ZonedDateTime

/**
 * Data class, represents the album, implements [Comparable] by comparing id
 *
 * @param name the name of band, should not be empty
 * @param coordinates the current position of band
 * @param numberOfParticipants the current number of participants, should be > 0
 * @param albumsCount the number of albums created by the band, can be null, should be > 0
 * @param description the description of band
 * @param genre the main genre of songs
 * @param bestAlbum the most listened album of band
 * @param id automatically generated value
 * @param creationTime the dateTime of band creation
 */
@Serializable
data class MusicBand(
    val name: String,
    val coordinates: Coordinates,
    val numberOfParticipants: Int,
    val albumsCount: Long?,
    val description: String,
    val genre: MusicGenre,
    val bestAlbum: Album?,
    val id: Int = generateId(arrayOf(name, description, genre.toString())),
    @Contextual
    val creationTime: ZonedDateTime = ZonedDateTime.now(),
    val owner: String? = null
) : Comparable<MusicBand> {

    override fun compareTo(other: MusicBand): Int {
        return id - other.id
    }
}

/**
 * Function used to generate id for MusicBand class
 *
 * @return hashCode based on name, description, genre
 */
fun generateId(array: Array<String>): Int {
    return array.contentHashCode()
}
