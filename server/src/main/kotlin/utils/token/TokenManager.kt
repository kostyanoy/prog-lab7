package utils.token

import FileManager
import exceptions.FileException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import java.security.MessageDigest
import java.util.*

/**
 * Implements [Tokenizer]
 *
 * @param encoder the instance of  [MessageDigest]
 * @param pathToKey the location of the secret key in os
 */
class TokenManager(
    private val encoder: MessageDigest,
    private val fileManager: FileManager,
    private val pathToKey: String
) : KoinComponent, Tokenizer {
    private val serializer = Json
    private val base64Encoder = Base64.getEncoder()
    private val base64Decoder = Base64.getDecoder()
    private val secretKey = loadSecretKey()

    override fun createToken(content: Content, expiresInMilli: Long): Token {
        val header = Header(encoder.algorithm, System.currentTimeMillis() + expiresInMilli)
        val sign = calculateSign(header, content)

        return Token(
            encode(serializer.encodeToString(header)),
            encode(serializer.encodeToString(content)),
            encode(sign)
        )
    }

    override fun checkToken(token: Token): Boolean {
        val header = getHeader(token)
        val content = getContent(token)
        return header.exp > System.currentTimeMillis() && calculateSign(header, content) == decode(token.sign)
    }

    override fun getHeader(token: Token): Header = serializer.decodeFromString(decode(token.header))

    override fun getContent(token: Token): Content = serializer.decodeFromString(decode(token.content))
    override fun getSign(token: Token): String = decode(token.sign)

    /**
     * Encodes encrypted header, content and secret key
     *
     * @param header the header of the token
     * @param content the content of the token
     * @return encoded sign
     */
    private fun calculateSign(header: Header, content: Content): String {
        val strHeader = serializer.encodeToString(header)
        val strContent = serializer.encodeToString(content)
        val string = strHeader + strContent + secretKey
        val hash = encoder.digest(string.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }

    /**
     * Loads secret key
     *
     * @return loaded secret key
     */
    private fun loadSecretKey(): String {
        return try {
            fileManager.readFile(pathToKey)
        } catch (e: FileException) {
            throw FileException("Не могу найти секретный ключ! (.key)")
        }
    }

    /**
     * @param str string that should be encoded in base64
     * @return encoded string
     */
    private fun encode(str: String): String = base64Encoder.encodeToString(str.toByteArray())

    /**
     * @param str string that should be decoded in base64
     * @return decoded string
     */
    private fun decode(str: String): String = String(base64Decoder.decode(str))

}