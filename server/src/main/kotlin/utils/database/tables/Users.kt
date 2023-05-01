package utils.database.tables

import org.jetbrains.exposed.dao.id.IntIdTable

/**
 * The representation of the users table in the database
 *
 * @property login the login of user. Must be <= 50 letters
 * @property password encrypted password of the user with SHA-384 algorithm
 */
object Users : IntIdTable("users") {
    val login = varchar("login", 50).uniqueIndex()
    val password = varchar("password", 96)
}