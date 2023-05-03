package utils.database.tables

import org.jetbrains.exposed.dao.id.IntIdTable
import utils.auth.UserStatus
import utils.database.tables.Users.login
import utils.database.tables.Users.password

/**
 * The representation of the users table in the database
 *
 * @property id key of user in database
 * @property login the login of user. Must be <= 50 letters
 * @property password encrypted password of the user with SHA-384 algorithm
 * @property status status of user
 */
object Users : IntIdTable("users") {
    val login = varchar("login", 50).uniqueIndex()
    val password = varchar("password", 96)
    val status = enumerationByName("status", 20, UserStatus::class)
}