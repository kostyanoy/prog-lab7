package utils

import FileManager
import data.MusicBand
import serialize.Serializer
import serialize.SerializeManager

/**
Implementation of [Saver] that saves and loads the collection of music bands to and from a file using [Serializer] and [FileManager].

@property pathToSaveFile The path to the file to save the collection to.
@property serializer The serializer to use for serializing and deserializing the collection.
@property fileManager The file manager to use for reading and writing the file.
 */
class FileSaver(
    private val pathToSaveFile: String = "save.txt",
    private val serializer: Serializer<LinkedHashMap<Int, MusicBand>> = SerializeManager(),
    private val fileManager: FileManager = FileManager()
) : Saver<LinkedHashMap<Int, MusicBand>> {
    override fun load(): LinkedHashMap<Int, MusicBand> =
        serializer.deserialize(fileManager.readFile(pathToSaveFile))

    override fun save(collection: LinkedHashMap<Int, MusicBand>) {
        fileManager.writeFile(pathToSaveFile, serializer.serialize(collection))
    }
}