package utils

import data.MusicBand
import exceptions.CommandException
import java.time.LocalDateTime

/**
 * The StorageManager class implements the Storage interface and represents a collection of MusicBand objects.
 *
 * It uses a LinkedHashMap to store the elements and provides methods to manipulate the collection, such as adding,
 * updating, removing, and clearing elements.
 */
class StorageManager : Storage<LinkedHashMap<Int, MusicBand>, Int, MusicBand> {
    private val date: LocalDateTime = LocalDateTime.now()
    private var musicBandCollection = LinkedHashMap<Int, MusicBand>()

    override fun getCollection(predicate: Map.Entry<Int, MusicBand>.() -> Boolean): LinkedHashMap<Int, MusicBand> =
        LinkedHashMap(musicBandCollection.filter(predicate))

    override fun getInfo(): String {
        return "Коллекция в памяти ${this.javaClass} \nтип: LinkedHashMap количество элементов  ${musicBandCollection.size} \nдата инициализации $date"
    }

    override fun insert(userId: Int, id: Int, element: MusicBand): Boolean {
        if (musicBandCollection[id] == null) {
            musicBandCollection[id] = element
            return true
        }
        throw CommandException("Элемент с таким ключом существует")
    }

    override fun update(userId: Int, id: Int, element: MusicBand): Boolean {
        if (musicBandCollection[id] == null)
            throw CommandException("Элемент с таким ключом не существует")
        musicBandCollection[id] = element
        return true
    }

    override fun clear(userId: Int): Boolean {
        musicBandCollection.clear()
        return true
    }

    override fun removeKey(userId: Int, id: Int): Boolean {
        if (musicBandCollection[id] == null)
            throw CommandException("Элемент с таким ключом не существует")
        musicBandCollection.remove(id)
        return true
    }
}


