package utils.database

import data.Album
import data.Coordinates
import data.MusicBand
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import utils.database.tables.Bands
import utils.database.tables.Users
import java.time.ZoneId

/**
 * Contains extension functions for more convenient use of Exposed library (insert, update, select) with [MusicBand]
 */


/**
 * Encapsulates assignment of the [MusicBand]'s properties to the columns in the statement
 */
fun MusicBand.toStatement(statement: UpdateBuilder<Int>) = statement.also {
    it[Bands.name] = this.name
    it[Bands.x] = this.coordinates.x
    it[Bands.y] = this.coordinates.y
    it[Bands.numberOfParticipants] = this.numberOfParticipants
    it[Bands.albumsCount] = this.albumsCount
    it[Bands.description] = this.description
    it[Bands.genre] = this.genre
    it[Bands.albumName] = this.bestAlbum?.name
    it[Bands.albumLength] = this.bestAlbum?.length
    it[Bands.creationTime] = this.creationTime.toInstant()
    it[Bands.zone] = this.creationTime.zone.id
}

/**
 * Encapsulates assignment of the columns' values to the properties of [MusicBand] from [ResultRow]
 */
fun ResultRow.toMusicBand(): MusicBand = MusicBand(
    id = this[Bands.bandId],
    name = this[Bands.name],
    coordinates = Coordinates(this[Bands.x], this[Bands.y]),
    numberOfParticipants = this[Bands.numberOfParticipants],
    albumsCount = this[Bands.albumsCount],
    description = this[Bands.description],
    genre = this[Bands.genre],
    bestAlbum = if (this[Bands.albumName] == null || this[Bands.albumLength] == null) null else Album(
        this[Bands.albumName]!!,
        this[Bands.albumLength]!!
    ),
    creationTime = this[Bands.creationTime].atZone(ZoneId.of(this[Bands.zone])),
    owner = this[Users.login]
)