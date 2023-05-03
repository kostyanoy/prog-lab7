package di
import FileManager
import Frame
import commands.CommandHistory
import data.MusicBand
import org.koin.dsl.module
import serialize.FrameSerializer
import serialize.Serializer
import utils.*
import serialize.SerializeManager

val serverModule = module {
    factory<Saver<LinkedHashMap<Int, MusicBand>>> {
        FileSaver("save.txt", serializer = get(), fileManager = get())
    }
    factory<Serializer<LinkedHashMap<Int, MusicBand>>> {
        SerializeManager()
    }
    factory<Serializer<Frame>> {
        FrameSerializer()
    }

    factory {
        FileManager()
    }
    single {
        CommandHistory()
    }
    single<Storage<LinkedHashMap<Int, MusicBand>, Int, MusicBand>> {
        StorageManager()
    }
    single {
        CommandManager()
    }
 }