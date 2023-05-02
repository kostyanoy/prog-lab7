import di.serverModule
import mu.KotlinLogging
import org.koin.core.context.GlobalContext.startKoin
import kotlin.concurrent.thread

/**
 * Main function that starts the application
 */
fun main(args: Array<String>) {
    startKoin {
        modules(serverModule)
    }

    val logger = KotlinLogging.logger {}

    var serverPort = 2229
    if (args.isNotEmpty()) {
        serverPort = args[0].toIntOrNull() ?: serverPort
    }

    logger.info { "Выбран порт: $serverPort" }

    val server = ServerApp("localhost", serverPort)

    val thread = thread {
        while (true) {
            when (readlnOrNull()) {
                "exit" -> {
                    server.stop()
                    logger.info { "Сервер закрылся" }
                    break
                }
            }
        }
    }
    server.start()
    thread.join()
}


