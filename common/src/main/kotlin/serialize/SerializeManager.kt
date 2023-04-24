package serialize

import data.MusicBand
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual

/**
 * Implements [Serializer] interface with JSON serialization for [LinkedHashMap]
 */
class SerializeManager : Serializer<LinkedHashMap<Int, MusicBand>> {
    private val module = SerializersModule {
        contextual(KZonedDateTimeSerializer)
    }

    private val serializer = Json { serializersModule = module }

    override fun serialize(collection: LinkedHashMap<Int, MusicBand>) =
        serializer.encodeToString(collection)

    override fun deserialize(serialized: String) =
        serializer.decodeFromString<LinkedHashMap<Int, MusicBand>>(serialized)
}