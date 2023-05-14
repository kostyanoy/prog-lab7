package utils.database

import FileManager
import data.Coordinates
import data.MusicBand
import data.MusicGenre
import exceptions.CommandException
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.selectAll
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import utils.auth.AuthManager
import utils.auth.EncryptManager
import utils.auth.token.TokenManager
import utils.database.tables.Bands
import utils.database.tables.Users
import java.security.MessageDigest

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DatabaseTests {

    private val fileManager = FileManager()
    private val encrypter = EncryptManager(MessageDigest.getInstance("SHA-384"), fileManager, "../.key")
    private val tokenizer = TokenManager(encrypter)
    private val database = DatabaseManager("localhost", "5432", "template1", fileManager, "../.pgpass")
    private val dbManager = DBStorageManager(database)
    private val authManager = AuthManager(tokenizer, encrypter, database)

    private val band1 = MusicBand("name1", Coordinates(1.0F, 1.0), 1, 1, "desc1", MusicGenre.SOUL, null)
    private val band2 = MusicBand("name2", Coordinates(2.0F, 2.0), 2, 2, "desc2", MusicGenre.HIP_HOP, null)


    @BeforeAll
    fun init() {
        database.updateTables(Bands, Users)
    }

    @BeforeEach
    @AfterAll
    fun clear() {
        database.makeTransaction {
            Bands.deleteAll()
            Users.deleteAll()
        }
    }

    @AfterAll
    fun closeConnection() {
        database.close()
    }

    // useful functions for db tests
    private fun createUser(login: String = "new", password: String = "pass"): Int {
        val token = authManager.register(login, password)
        return tokenizer.getContent(token).userId
    }

    private fun Table.getAll(): List<ResultRow> = database.makeTransaction {
        selectAll().toList()
    }

    // auth tests
    @Test
    fun `Register users`() {
        authManager.register("new", "pass")

        val users = Users.getAll()

        assertEquals(1, users.count())
        assertEquals("new", users[0][Users.login])
        assertNotEquals("pass", users[0][Users.password])
        assertThrows<CommandException> {
            authManager.register("new", "pass")
        }
    }

    @Test
    fun `Login users`() {
        authManager.register("new", "pass")

        val users = Users.getAll()

        assertEquals(1, users.count())
        assertEquals("new", users[0][Users.login])
        assertNotEquals("pass", users[0][Users.password])
        assertThrows<CommandException> {
            authManager.login("new", "wrong")
        }
        assertThrows<CommandException> {
            authManager.login("wrong", "pass")
        }
    }

    // DBManager tests

    @Test
    fun `Must insert`() {
        val id = createUser()

        dbManager.insert(id, 1, band1)

        val bands = Bands.getAll()

        assertEquals(1, bands.count())
        assertEquals(id, bands[0][Bands.userId])
        assertThrows<CommandException> {
            dbManager.insert(id, 1, band1)
        }
    }

    @Test
    fun `Must clear`() {
        val id = createUser()

        dbManager.insert(id, 1, band1)

        val bandsBefore = Bands.getAll()

        assertEquals(1, bandsBefore.count())

        dbManager.clear(id)

        val bandsAfter = Bands.getAll()

        assertEquals(0, bandsAfter.count())

    }

    @Test
    fun `Must remove key`() {
        val id = createUser()

        dbManager.insert(id, 1, band1)
        dbManager.insert(id, 2, band2)

        val bandsBefore = Bands.getAll()

        assertEquals(2, bandsBefore.count())

        dbManager.removeKey(id, 2)

        val bandsAfter = Bands.getAll()

        assertEquals(1, bandsAfter.count())
        assertEquals("name1", bandsAfter[0][Bands.name])

    }

    @Test
    fun `Must update`() {
        val id = createUser()

        dbManager.insert(id, 1, band1)

        val bandsBefore = Bands.getAll()

        assertEquals(1, bandsBefore.count())
        assertEquals("name1", bandsBefore[0][Bands.name])

        dbManager.update(id, 1, band2)

        val bandsAfter = Bands.getAll()

        assertEquals(1, bandsAfter.count())
        assertEquals("name2", bandsAfter[0][Bands.name])
    }
}