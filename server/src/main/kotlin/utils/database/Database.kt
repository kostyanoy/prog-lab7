package utils.database

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
}