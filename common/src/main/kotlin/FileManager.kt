import exceptions.FileException
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.util.*

/**
 * Class used to read and write text files
 */
class FileManager {
    /**
     * Writes [text] to the file located at [path] in the OS
     *
     * @param path the path to the file in the OS
     * @param text the content of the file
     */
    fun writeFile(path: String, text: String) {
        FileOutputStream(path).use { fos ->
            OutputStreamWriter(fos, Charsets.UTF_8).use { osw ->
                BufferedWriter(osw).use { bf -> bf.write(text) }
            }
        }
    }

    /**
     * Reads the content of the file located at [path] in the OS
     *
     * @param path the path to the file in the OS
     * @throws [FileException] if the file can't be opened or found
     */
    fun readFile(path: String): String {
        if (!File(path).canRead()) {
            println(System.getProperty("user.dir"))
            throw FileException("Не получается открыть файл: $path")
        }
        val scanner = Scanner(File(path))
        return buildString {
            while (scanner.hasNextLine()) {
                append(scanner.nextLine())
                if (scanner.hasNextLine()) append("\n")
            }
        }
    }
}