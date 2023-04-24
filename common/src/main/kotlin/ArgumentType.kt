import kotlinx.serialization.Serializable

/**
 * Enum represents possible arguments of the commands
 */
@Serializable
enum class ArgumentType {
    INT, STRING, GENRE, MUSIC_BAND
}