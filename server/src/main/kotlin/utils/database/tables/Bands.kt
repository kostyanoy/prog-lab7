package utils.database.tables

import data.MusicGenre
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp

/**
 * The representation of the users table in the database
 *
 * All properties represent expanded MusicBand's properties
 * @property creationTime saves ZonedDateTime as Instant
 * @property zone contain id of the Zone
 * @property userId references to [Users] table
 */
object Bands : IntIdTable("bands") {
    val name = varchar("name", 255)
    val x = float("x")
    val y = double("y")
    val numberOfParticipants = integer("numberOfParticipants")
    val albumsCount = long("albumsCount").nullable()
    val description = text("description")
    val genre = enumerationByName("genre", 20, MusicGenre::class)
    val albumName = varchar("best_album_name", 255).nullable()
    val albumLength = long("best_album_length").nullable()
    val creationTime = timestamp("creationTime")
    val zone = varchar("zone", 50)
    val bandId = integer("band_id").autoIncrement().uniqueIndex()
    val userId = integer("user_id").references(Users.id).nullable()
}