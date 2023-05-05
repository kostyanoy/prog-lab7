//import di.gatewayModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mu.KotlinLogging

/**
 * Main function that starts the application
 */
fun main(args: Array<String>) {

    val logger = KotlinLogging.logger {}

    var clientPort = 2228
    var serverPort = 2229
    if (args.isNotEmpty()) {
        clientPort = args[0].toIntOrNull() ?: clientPort
        serverPort = args[1].toIntOrNull() ?: serverPort
    }

    logger.info { "Выбраны порты: $clientPort, $serverPort" }

    val gateway = GatewayLBService(clientPort, serverPort)

    val job = CoroutineScope(Dispatchers.IO).launch {
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
    job.cancel()
}