package utils.database

import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import utils.database.tables.Bands
import utils.database.tables.Users
import java.sql.Connection

/**
 * Interface for database wrappers
 */
interface Database {
    /**
     * @return connection from the connection pool. Connection must be closed after using
     */
    fun getConnection(): Connection

    /**
     * Closes the dataSource
     */
    fun close()

    fun updateTables() = getConnection().use {
        transaction{
            SchemaUtils.createMissingTablesAndColumns(Users, Bands)
        }
    }
}