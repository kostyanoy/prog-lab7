package utils.auth.token

import kotlinx.serialization.Serializable

/**
 * Represents header for [Token]
 *
 * @param alg the name of encrypting algorithm
 * @param exp the time in milliseconds when the token expires
 */
@Serializable
data class Header(val alg: String, val exp: Long)