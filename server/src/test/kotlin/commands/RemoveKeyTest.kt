package commands

import data.Coordinates
import data.MusicBand
import data.MusicGenre
import exceptions.CommandException
import org.junit.jupiter.api.Assertions
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

internal class RemoveKeyTest : KoinTest {
    private val m1 = MusicBand("name1", Coordinates(1.0F, 1.0), 1, 1, "", MusicGenre.HIP_HOP, null)
    private val m2 = MusicBand("name2", Coordinates(2.0F, 2.0), 2, 2, "", MusicGenre.POST_PUNK, null)

    private val storage: Storage<LinkedHashMap<Int, MusicBand>, Int, MusicBand> by inject()

    @JvmField
    @RegisterExtension
    val koinTestExtension = KoinTestExtension.create {
        modules(module {
            single<Storage<LinkedHashMap<Int, MusicBand>, Int, MusicBand>> { StorageManager() }
        })
    }

    @Test
    fun `Remove key from non-empty collection`() {
        storage.insert(1, 1, m1)
        storage.insert(1, 2, m2)

        val removeKeyCommand = RemoveKey()
        removeKeyCommand.execute(arrayOf(2, Content(1, UserStatus.USER)))

        assertEquals(1, storage.getCollection { true }.count())
        assertEquals(m1, storage.getCollection { true }[1])
        Assertions.assertFalse(storage.getCollection { true }.containsKey(2))
    }

    @Test
    fun `Remove key from empty collection fails`() {
        val removeKeyCommand = RemoveKey()

        assertThrows<CommandException> { removeKeyCommand.execute(arrayOf(2, Content(1, UserStatus.USER))) }
    }
}