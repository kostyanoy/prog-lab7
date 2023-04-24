package utils

import data.MusicBand
import java.time.LocalDateTime
/**
The StorageManager class implements the Storage interface and represents a collection of MusicBand objects.

It uses a LinkedHashMap to store the elements and provides methods to manipulate the collection, such as adding,
updating, removing, and clearing elements.
 */
class StorageManager : Storage<LinkedHashMap<Int, MusicBand>, Int, MusicBand> {
    private val date: LocalDateTime = LocalDateTime.now()
    val musicBandCollection = LinkedHashMap<Int, MusicBand>()

    override fun getCollection(predicate: Map.Entry<Int, MusicBand>.() -> Boolean): LinkedHashMap<Int, MusicBand> =
        LinkedHashMap(musicBandCollection.filter(predicate))

    override fun getInfo(): String {
        return "Коллекция  ${this.javaClass} \nтип: LinkedHashMap количество элементов  ${musicBandCollection.size} \nдата инициализации $date"
    }

    override fun insert(id: Int, element: MusicBand) {
        musicBandCollection[id] = element
    }

    override fun update(id: Int, element: MusicBand) {
        musicBandCollection[id] = element
    }

    override fun clear() {
        musicBandCollection.clear()
    }

    override fun removeKey(id: Int) {
        musicBandCollection.remove(id)
    }
}


