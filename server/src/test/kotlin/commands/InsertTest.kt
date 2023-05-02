package commands

import data.Coordinates
import data.MusicBand
import data.MusicGenre
import exceptions.CommandException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.core.component.inject
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.junit5.KoinTestExtension
import utils.Storage
import utils.StorageManager
import utils.auth.UserStatus
import utils.auth.token.Content

internal class InsertTest : KoinTest {
    private val m = MusicBand("name", Coordinates(1.0F, 1.0), 1, 1, "", MusicGenre.HIP_HOP, null)

    private val storage: Storage<LinkedHashMap<Int, MusicBand>, Int, MusicBand> by inject()

    @JvmField
    @RegisterExtension
    val koinTestExtension = KoinTestExtension.create {
        modules(module {
            single<Storage<LinkedHashMap<Int, MusicBand>, Int, MusicBand>> { StorageManager() }
        })
    }


    @Test
    fun `Insert MusicBand into empty collection`() {
        val insertCommand = Insert()
        insertCommand.execute(arrayOf(1, m, Content(1, UserStatus.USER)))

        assertEquals(m, storage.getCollection { true }[1])
        assertEquals(1, storage.getCollection { true }.count())
    }

    @Test
    fun `Insert Fails if exists`() {
        val insertCommand = Insert()
        insertCommand.execute(arrayOf(1, m, Content(1, UserStatus.USER)))

        assertThrows<CommandException> { insertCommand.execute(arrayOf(1, m, Content(1, UserStatus.USER))) }
    }
}