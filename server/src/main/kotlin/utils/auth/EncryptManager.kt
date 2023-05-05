package utils.auth

import FileManager
import exceptions.FileException
import java.security.MessageDigest

/**
 * Do encrypt stuff. Loads secret key from file
 *
 * @param encrypter [MessageDigest] instance with given algorithm
 * @param fileManager used to read secret key
 * @param pathToKey where key is
 */
class EncryptManager(
    private val encrypter: MessageDigest,
    private val fileManager: FileManager,
    private val pathToKey: String
) {
    private val secretKey = loadSecretKey()

    /**
     * Encrypts the [String] with salt
     *
     * @param text which must be encrypted
     * @return encrypted [String]
     */
    fun encrypt(text: String): String {
        val withSalt = text + secretKey
        val hash = encrypter.digest(withSalt.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }

    /**
     * @return name of the encrypting algorithm
     */
    fun getAlgorithm(): String = encrypter.algorithm

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
}