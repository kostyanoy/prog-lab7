package utils.auth.token

/**
 * Represents a token for client-server architecture
 *
 * Stores [header], [content], [sign] encoded in base64
 *
 * @param header the [Header] encoded in base64, have useful info about token
 * @param content the useful info about user encoded in base64
 * @param sign the sign based on header and content encrypted by hashing algorithm and encoded in base64
 */
data class Token(val header: String, val content: String, val sign: String) {
    companion object {
        /**
         * Converts token from [String] to [Token]
         *
         * @param token the token in string
         * @return [Token] from given string
         * @throws IllegalArgumentException if string can't be converted to token
         */
        fun parse(token: String): Token {
            if (token.count { it == '.' } != 2)
                throw IllegalArgumentException("Строка не соответствует токену")
            val parts = token.split('.')
            return Token(parts[0], parts[1], parts[2])
        }
    }

    /**
     * @return token like header.content.sigh
     */
    override fun toString(): String = "$header.$content.$sign"
}