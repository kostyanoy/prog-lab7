package serialize

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Contextual serializer for [Throwable]
 */
object KThrowableSerializer : KSerializer<Throwable> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Throwable", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Throwable) =
        //serializes only message. Any exception will become Throwable :)
        encoder.encodeString(value.message.toString())

    override fun deserialize(decoder: Decoder): Throwable =
        Throwable(decoder.decodeString())
}