package utils.auth

import exceptions.CommandException
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import utils.auth.token.Content
import utils.auth.token.Token
import utils.auth.token.Tokenizer
import utils.database.Database
import utils.database.tables.Users
import java.sql.Connection
import java.sql.SQLException

/**
 * Do authorizations: register, login and generates tokens
 *
 * @param tokenManager generates tokens
 * @param encrypter do encypt of the passwords
 * @param database gives connections to database
 */
class AuthManager(
    private val tokenManager: Tokenizer,
    private val encrypter: EncryptManager,
    private val database: Database
) {

    /**
     * Check if login is used and creates new users.
     *
     * @param login the login of user
     * @param password the password of user
     * @return temporary token
     * @throws CommandException if login exists
     */
    fun register(login: String, password: String, userStatus: UserStatus = UserStatus.USER): Token {
        if (isUserExists(login)) {
            throw CommandException("Пользователь с таким логином уже существует")
        }
        val userId = getConnection().use {
            transaction {
                Users.insertAndGetId {
                    it[Users.login] = login
                    it[Users.password] = encrypter.encrypt(password)
                    it[status] = userStatus
                }
            }
        }
        return tokenManager.createToken(Content(userId.value, userStatus))
    }

    /**
     * Check if login exists and checks password.
     *
     * @param login the login of user
     * @param password the password of user
     * @return temporary token
     * @throws CommandException if login not exists
     */
    fun login(login: String, password: String): Token {
        if (!isUserExists(login)) {
            throw CommandException("Пользователя с таким логином не существует")
        }
        val encryptedPassword = encrypter.encrypt(password)
        val user = getConnection().use {
            transaction {
                Users.select { (Users.login eq login) and (Users.password eq encryptedPassword) }
                    .single()
            }
        }
        return tokenManager.createToken(Content(user[Users.id].value, user[Users.status]))
    }

    /**
     * Checks if login used
     *
     * @param login of user
     * @return true if login used
     */
    fun isUserExists(login: String): Boolean = getConnection().use {
        val user = transaction {
            Users.select { Users.login eq login }.singleOrNull()
        }
        return user != null
    }

    /**
     * @return connection to database
     * @throws CommandException if no connections
     */
    private fun getConnection(): Connection {
        try {
            return database.getConnection()
        } catch (e: SQLException) {
            throw CommandException("Не удалось получить подключение к базе данных + ${e.message}")
        }
    }
}