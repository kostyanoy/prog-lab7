package utils
/**
 * Interface for interacting with the collection
 */
interface Storage<T, K, V> {
    fun getCollection(predicate: Map.Entry<K, V>.() -> Boolean): T
    fun removeKey(id: K)

    fun getInfo(): String
    fun insert(id: K, element: V)
    fun update(id: K, element: V)
    fun clear()
}