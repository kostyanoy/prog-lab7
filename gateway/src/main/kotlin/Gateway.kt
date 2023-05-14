import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import kotlin.concurrent.thread

/**
 * Main function that starts the application
 */
fun main(args: Array<String>) = runBlocking(Dispatchers.IO) {
    val logger = KotlinLogging.logger {}

    var clientPort = 2228
    var serverPort = 2229
    if (args.isNotEmpty()) {
        clientPort = args[0].toIntOrNull() ?: clientPort
        serverPort = args[1].toIntOrNull() ?: serverPort
    }

    logger.info { "Выбраны порты: $clientPort, $serverPort" }

    val gateway = GatewayLBService(clientPort, serverPort)

    val console = thread {
        while (true) {
            when (readlnOrNull()) {
                "exit" -> {
                    gateway.stop()
                    logger.info { "Шлюз закрылся" }
                    break
                }
            }
        }
    }
    gateway.start()
    console.join()
}