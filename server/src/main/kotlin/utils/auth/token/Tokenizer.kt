package utils.auth.token

/**
 * Interface for different token generators
 */
interface Tokenizer {
    /**
     * Creates token from given content
     *
     * @param content the useful info about user
     * @param expiresInMilli the time in which token expires from now
     * @return created token
     */
    fun createToken(content: Content, expiresInMilli: Long = 1800000): Token

    /**
     * Check if the token wasn't modified and is not expired
     *
     * @param token the checked token
     * @return true if token is correct
     */
    fun checkToken(token: Token): Boolean

    /**
     * Gets header from token
     *
     * @param token token with encoded info
     * @return [Header]
     */
    fun getHeader(token: Token): Header

    /**
     * Gets content from token
     *
     * @param token token with encoded info
     * @return [Content]
     */
    fun getContent(token: Token): Content
    /**
     * Gets sign from token
     *
     * @param token token with encoded info
     * @return [String] represented encrypted sign
     */
    fun getSign(token: Token): String
}