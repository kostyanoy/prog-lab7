import di.gatewayModule
import mu.KotlinLogging
import org.koin.core.context.GlobalContext.startKoin
import kotlin.concurrent.thread

/**
 * Main function that starts the application
 */
fun main(args: Array<String>) {
    startKoin {
        modules(gatewayModule)
    }

    val logger = KotlinLogging.logger {}

    var clientPort = 2228
    var serverPort = 2229
    if (args.isNotEmpty()){
        clientPort = args[0].toIntOrNull() ?: clientPort
        serverPort = args[1].toIntOrNull() ?: serverPort
    }

    logger.info { "Выбраны порты: $clientPort, $serverPort" }

    val gateway = GatewayLBService(clientPort, serverPort)

    val thread = thread {
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
    thread.join()
}