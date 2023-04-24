package data

import kotlinx.serialization.Serializable

/**
 * Enum contains the genres of songs that can be used by [MusicBand]
 */
@Serializable
enum class MusicGenre {
    PROGRESSIVE_ROCK, HIP_HOP, PSYCHEDELIC_CLOUD_RAP, SOUL, POST_PUNK;

    companion object {
        /**
         * Decorates the [valueOf] by comparing with [values]
         *
         * @param name take the string that should be converted to [MusicGenre]
         * @return [MusicGenre] or null if string can't be converted
         */
        fun valueOfOrNull(name: String): MusicGenre? {
            return values().firstOrNull { it.name == name }
        }
    }
}