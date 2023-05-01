package utils.database

import data.MusicBand
import exceptions.CommandException
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import utils.Storage
import utils.database.tables.Bands
import java.sql.Connection
import java.sql.SQLException
import java.time.LocalDateTime
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * Works with the collection in the database and stored in the memory
 *
 * @param database
 */
class DBStorageManager(private val database: Database) : Storage<LinkedHashMap<Int, MusicBand>, Int, MusicBand> {
    private val date: LocalDateTime = LocalDateTime.now()
    private var musicBandCollection = LinkedHashMap<Int, MusicBand>()
    private val lock = ReentrantReadWriteLock()

    override fun getCollection(predicate: Map.Entry<Int, MusicBand>.() -> Boolean): LinkedHashMap<Int, MusicBand> {
        updateCollection()
        return lock.read {
            LinkedHashMap(musicBandCollection.filter(predicate))
        }
    }

    override fun getInfo(): String {
        return "Коллекция в памяти ${this.javaClass} \nтип: LinkedHashMap количество элементов  ${musicBandCollection.size} \nдата инициализации $date"
    }

    override fun removeKey(userId: Int, id: Int): Boolean = getConnection().use {
        transaction {
            val band = Bands.select { Bands.id eq id }.singleOrNull()
                ?: throw CommandException("Не существует такого ключа")
            if (band[Bands.userId] != userId) {
                throw CommandException("Эта банда принадлежит другому пользователю")
            }
            Bands.deleteWhere { Bands.id eq id }
        }
        true
    }

    override fun clear(userId: Int): Boolean = getConnection().use {
        transaction {
            Bands.deleteWhere { Bands.userId eq userId }
        }
        true
    }

    override fun update(userId: Int, id: Int, element: MusicBand): Boolean = getConnection().use {
        transaction {
            val band = Bands.select { Bands.id eq id }.singleOrNull()
                ?: throw CommandException("Не существует такого ключа")
            if (band[Bands.userId] != userId) {
                throw CommandException("Эта банда принадлежит другому пользователю")
            }
            Bands.update({ Bands.id eq id }) {
                element.toUpdateStatement(it)
            }
        }
        true
    }

    override fun insert(userId: Int, id: Int, element: MusicBand): Boolean = getConnection().use {
        transaction {
            val band = Bands.select { Bands.id eq id }.singleOrNull()
            if (band != null) {
                throw CommandException("Не существует такого ключа")
            }
            Bands.insert {
                element.toInsertStatement(it)
                it[Bands.userId] = userId
            }
        }
        return true
    }

    private fun updateCollection() {
        val bands = LinkedHashMap<Int, MusicBand>()
        transaction {
            Bands.selectAll().map {
                bands[it[Bands.id].value] = it.toMusicBand()
            }
        }
        lock.write {
            musicBandCollection = bands
        }
    }

    private fun getConnection(): Connection {
        try {
            return database.getConnection()
        } catch (e: SQLException) {
            throw CommandException("Не удалось получить подключение к базе данных + ${e.message}")
        }
    }
}