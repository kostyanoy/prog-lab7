package utils.database

import exceptions.CommandException
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection
import java.sql.SQLException

/**
 * @return connection to database
 * @throws CommandException if no connections
 */
fun Database.verifiedConnection(): Connection {
    try {
        return getConnection()
    } catch (e: SQLException) {
        throw CommandException("Не удалось получить подключение к базе данных + ${e.message}")
    }
}

/**
 * Make transaction with its own connection
 *
 * @param statement the transaction statements
 */
fun <T> Database.makeTransaction(statement: Transaction.() -> T) =
    verifiedConnection().use { transaction(statement = statement) }

/**
 * Creates gives tables in database
 *
 * @param tables the tables needed to create
 */
fun Database.updateTables(vararg tables: Table) = makeTransaction {
    SchemaUtils.createMissingTablesAndColumns(*tables)
}