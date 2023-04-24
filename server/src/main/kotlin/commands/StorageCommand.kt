package commands

import Command
import data.MusicBand
import org.koin.core.component.inject
import utils.Storage
/**
Abstract class that represents a command that operates on a storage object.
 */
abstract class StorageCommand : Command() {
    val storage: Storage<LinkedHashMap<Int, MusicBand>, Int, MusicBand> by inject()
}