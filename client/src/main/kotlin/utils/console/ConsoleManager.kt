package utils.console

import utils.ReaderWriter
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.Charset

/**
 * Implements the [ReaderWriter] by interacting with the console with build-in methods
 */
class ConsoleManager : ReaderWriter {
    private val inputStream: InputStream = System.`in`
    private val reader = InputStreamReader(inputStream, Charset.forName("UTF-8"))

    override fun readLine(): String = BufferedReader(reader).readLine().trim()
    override fun readPassword(): String {
        if (System.console() == null)
            return readLine()
        return String(System.console().readPassword()).trim()
    }

    override fun writeLine(text: String) = println(text)
    override fun write(text: String) = print(text)

    override fun getValidatedValue(message: String, validator: (String) -> Boolean): String {
        write(message)
        while (true) {
            val userInput = readLine()
            if (validator(userInput)) {
                return userInput
            }

            write("Попробуйте ещё раз: ")
        }
    }
}