package utils

/**
 * Saves and loads [LinkedHashMap] collection
 */
interface Saver<T> {
    /**
     * Save [collection] for loading in the future
     *
     * @param collection that must be saved
     */
    fun save(collection: T)

    /**
     * Loads saved [LinkedHashMap] collection
     *
     * @return loaded previously saved collection
     */
    fun load(): T
}