import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
/**
 * Class used to transfer information between server and client
 *
 * @param type the [FrameType] which the frame is
 */
@Serializable
class Frame(val type: FrameType) {

    /**
     * Context of the frame
     */
    val body = mutableMapOf<String, @Contextual Any>()

    /**
     * USed to add parameters to the [body]
     */
    fun setValue(key: String, value: Any) {
        body[key] = value
    }
}
