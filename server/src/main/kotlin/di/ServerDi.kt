package di

import FileManager
import ServerApp
import commands.CommandHistory
import data.MusicBand
import org.koin.dsl.module
import serialize.SerializeManager
import serialize.Serializer
import utils.CommandManager
import utils.FileSaver
import utils.Saver
import utils.Storage
import utils.database.DBStorageManager
import utils.database.Database
import utils.database.DatabaseManager
import utils.token.TokenManager
import utils.token.Tokenizer
import java.security.MessageDigest

val serverModule = module {
    // files
    single<Saver<LinkedHashMap<Int, MusicBand>>> {
        FileSaver("save.txt", serializer = get(), fileManager = get())
    }
    single {
        FileManager()
    }

    // serialization
    single<Serializer<LinkedHashMap<Int, MusicBand>>> {
        SerializeManager()
    }

    // commands
    single {
        CommandHistory()
    }
    single {
        CommandManager()
    }

    //token
    single {
        MessageDigest.getInstance("SHA-384")
    }
    single<Tokenizer> {
        TokenManager(encoder = get(), fileManager = get(), ".key")
    }

    // database
    single<Storage<LinkedHashMap<Int, MusicBand>, Int, MusicBand>> {
        DBStorageManager(database = get())
    }
    single<Database> {
        DatabaseManager("pg", "5432", "studs")
    }

    // server
    single {
        ServerApp(2228)
    }

}