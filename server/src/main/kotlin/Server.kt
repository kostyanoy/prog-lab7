import di.serverModule
import mu.KotlinLogging
import org.koin.core.context.GlobalContext.startKoin
import kotlin.concurrent.thread

/**
 * Main function that starts the server application and listens to commands from the console.
 * The application can be stopped using the 'exit' command, and db tables updates by update.
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

    val console = thread {
        while (true) {
            when (readlnOrNull()) {
                "exit" -> {
                    server.stop()
                    logger.info { "Сервер закрылся" }
                    break
                }

                "update" -> server.updateTables()
            }
        }
    }
    server.start()
    console.join()
}