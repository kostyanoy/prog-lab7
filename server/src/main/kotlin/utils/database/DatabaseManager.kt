package utils.database

import FileManager
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import mu.KotlinLogging
import org.jetbrains.exposed.sql.Database
import java.sql.Connection
import utils.database.Database as MyDatabase

/**
 * Works with PostreSQL. Reads username and password from file. Uses HikariCP for connection pool
 *
 * @param host the host of database on the server
 * @param port the port of database
 * @param db the name of the database on the server
 * @param fileManager reads files
 * @param path path to .pgpass
 */
class DatabaseManager(
    private val host: String = "localhost",
    private val port: String = "5432",
    private val db: String = "studs",
    private val fileManager: FileManager,
    private val path: String = ".pgpass"
) : MyDatabase {
    private val config = HikariConfig().apply {
        driverClassName = "org.postgresql.Driver"
        jdbcUrl = "jdbc:postgresql://$host:$port/$db"
        username = parseUser()
        password = parsePassword()
    }
    private var dataSource: HikariDataSource? = null

    private val logger = KotlinLogging.logger { }
    
    override fun getConnection(): Connection = getDataSource().connection

    override fun close() {
        dataSource?.close()
        logger.info { "Закрыл подключение к базе данных" }
    }

    /**
     * Creates [HikariDataSource] with the config parameters if there is none
     *
     * @return dataSource
     */
    private fun getDataSource(): HikariDataSource {
        if (dataSource == null || dataSource!!.isClosed) {
            logger.info { "Открыл подключение к базе данных" }
            dataSource = HikariDataSource(config)
            Database.connect(dataSource!!)
        }
        return dataSource!!
    }

    /**
     * Parses username from given file
     *
     * @return username
     */
    private fun parseUser(file: String = path): String {
        val line = fileManager.readFile(file).split(":")
        return line[3]
    }

    /**
     * Parses password from given file
     *
     * @return password
     */
    private fun parsePassword(file: String = path): String {
        val line = fileManager.readFile(file).split(":")
        return line[4]
    }
}

