package di

import FileManager
import Frame
import org.koin.dsl.module
import serialize.FrameSerializer
import serialize.Serializer
import utils.*
import utils.console.ConsoleManager
import utils.state.AuthState

/**
 * Koin module for the client part
 */
val clientModule = module {
    single<ReaderWriter> {
        ConsoleManager()
    }

    single<Serializer<Frame>> {
        FrameSerializer()
    }

    single<Validator> {
        ValidationManager(interactor = get(), userManager = get())
    }

    single {
        CommandManager()
    }

    single {
        FileManager()
    }

    single {
        FrameSerializer()
    }

    single<Interactor> {
        InteractionManager(userManager = get(), fileManager = get(), commandManager = get(), AuthState())
    }
}