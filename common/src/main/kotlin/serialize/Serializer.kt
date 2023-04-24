package serialize

/**
 * Interface for serialization of [LinkedHashMap]
 */
interface Serializer<T> {
    /**
     * Serialize object to [String]
     *
     * @param collection the object that will be serialized
     * @return serialized [String]
     */
    fun serialize(collection: T): String

    /**
     * Deserialize object from [String]
     *
     * @param serialized the serialized [String] that will be deserialized
     * @return deserialized object
     */
    fun deserialize(serialized: String): T
}