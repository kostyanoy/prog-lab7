package utils

import data.Coordinates
import data.MusicBand
import data.MusicGenre
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.ZonedDateTime

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class FileSaverTest {
    private val path = "src/test/kotlin/save.txt"
    private val fileSaver = FileSaver(path)
    private val band1 = MusicBand(
        "s",
        Coordinates(1.0f, 2.0),
        1,
        1,
        "s",
        MusicGenre.HIP_HOP,
        null,
        1,
        ZonedDateTime.now()
    )
    private val band2 = band1.copy(id = 2)
    private val originalCollection = linkedMapOf(1 to band1, 2 to band2)

    @Test
    fun `Collection should be equal before and after serialization to file`() {
        fileSaver.save(originalCollection)
        assertEquals(originalCollection, fileSaver.load())
    }
}