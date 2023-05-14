package utils.auth

import exceptions.CommandException
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import utils.auth.token.Content
import utils.auth.token.Token
import utils.auth.token.Tokenizer
import utils.database.Database
import utils.database.makeTransaction
import utils.database.tables.Users

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
        val encryptedPassword = encrypter.encrypt(password)
        val userId = database.makeTransaction {
            Users.insertAndGetId {
                it[Users.login] = login
                it[Users.password] = encryptedPassword
                it[status] = userStatus
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
        val encryptedPassword = encrypter.encrypt(password)
        val user = database.makeTransaction {
            Users.select { (Users.login eq login) and (Users.password eq encryptedPassword) }
                .singleOrNull()
        } ?: throw CommandException("Неправильный логин или пароль")

        return tokenManager.createToken(Content(user[Users.id].value, user[Users.status]))
    }

    /**
     * Checks if login used
     *
     * @param login of user
     * @return true if login used
     */
    fun isUserExists(login: String): Boolean {
        val user = database.makeTransaction {
            Users.select { Users.login eq login }.singleOrNull()
        }
        return user != null
    }
}