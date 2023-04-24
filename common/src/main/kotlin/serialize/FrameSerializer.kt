package serialize

import Frame
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual

/**
 * Implements [Serializer] interface with JSON serialization for Frames
 */
class FrameSerializer : Serializer<Frame> {
    // needed contextual serializer
    private val module = SerializersModule {
        contextual(KAnySerializer)
        contextual(KZonedDateTimeSerializer)
        contextual(KThrowableSerializer)
    }

    private val serializer = Json { serializersModule = module }

    override fun serialize(collection: Frame): String = serializer.encodeToString(collection)
    override fun deserialize(serialized: String): Frame = serializer.decodeFromString(serialized)

}





