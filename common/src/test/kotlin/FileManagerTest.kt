import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class FileManagerTest {
    private val path = "src/test/kotlin/save.txt"
    private val fileManager = FileManager()


    @Test
    fun `Written text to file should be equal to original`() {
        val originalText = """
          123
          456
        """.trimIndent()

        fileManager.writeFile(path, originalText)
        assertEquals(originalText, fileManager.readFile(path))
    }

}