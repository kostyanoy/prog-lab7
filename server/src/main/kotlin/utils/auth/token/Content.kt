package utils.auth.token

import kotlinx.serialization.Serializable
import utils.auth.UserStatus

/**
 * Represents content for [Token]
 *
 * @param userId the id of user
 * @param status the status of user in system
 */
@Serializable
data class Content(val userId: Int, val status: UserStatus)