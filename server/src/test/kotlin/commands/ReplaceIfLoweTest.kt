package commands

import data.Coordinates
import data.MusicBand
import data.MusicGenre
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.core.component.inject
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.junit5.KoinTestExtension
import utils.Storage
import utils.StorageManager
import utils.auth.UserStatus
import utils.auth.token.Content

internal class ReplaceIfLoweTest : KoinTest {
    private val m1 = MusicBand("name1", Coordinates(1.0F, 1.0), 1, 1, "", MusicGenre.HIP_HOP, null, id = 1)
    private val m2 = MusicBand("name2", Coordinates(2.0F, 2.0), 2, 2, "", MusicGenre.POST_PUNK, null, id = 2)

    private val storage: Storage<LinkedHashMap<Int, MusicBand>, Int, MusicBand> by inject()

    @JvmField
    @RegisterExtension
    val koinTestExtension = KoinTestExtension.create {
        modules(module {
            single<Storage<LinkedHashMap<Int, MusicBand>, Int, MusicBand>> { StorageManager() }
        })
    }

    @Test
    fun `Replace MusicBand if user element is lower than current element`() {
        storage.insert(1, 1, m2)

        val replaceIfLowerCommand = ReplaceIfLowe()
        replaceIfLowerCommand.execute(arrayOf(1, m1, Content(1, UserStatus.USER)))

        assertEquals(1, storage.getCollection { true }.count())
        assertEquals(m1, storage.getCollection { true }[1])
    }
}