package utils

/**
 * Interface for interacting with the collection
 */
interface Storage<T, K, V> {
    /**
     * @return information about stored collection
     */
    fun getInfo(): String

    /**
     * Get elements in the collection
     *
     * @param predicate used to filter values in collection
     * @return filtered collection of elements
     */
    fun getCollection(predicate: Map.Entry<K, V>.() -> Boolean): T

    /**
     * Removes element from collection
     *
     * @param userId the id of current user
     * @param id of the element which should be removed
     * @return true if was successful
     */
    fun removeKey(userId: Int, id: K): Boolean

    /**
     * Inserts element to the collection
     *
     * @param userId the id of current user
     * @param id of the element which should be inserted
     * @param element which will have [id] in the collection
     * @return true if was successful
     */
    fun insert(userId: Int, id: K, element: V): Boolean

    /**
     * Updates element in the collection
     *
     * @param userId the id of current user
     * @param id of the element which should be updated
     * @param element which have [id] in the collection
     * @return true if was successful
     */
    fun update(userId: Int, id: K, element: V): Boolean

    /**
     * Removes all elements of the user
     *
     * @param userId the id of current user
     * @return true if was successful
     */
    fun clear(userId: Int): Boolean
}