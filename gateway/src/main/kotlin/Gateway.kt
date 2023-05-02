import di.gatewayModule
import org.koin.core.context.GlobalContext.startKoin
import kotlin.concurrent.thread

fun main(args: Array<String>) {
    var serverPort = 2229
    var clientPort = 2228
    if (args.isNotEmpty()){
        serverPort = args[0].toIntOrNull() ?: serverPort
        clientPort = args[0].toIntOrNull() ?: clientPort
    }
    val gateway = GatewayLBService(clientPort, serverPort)

    val thread = thread {
        while (true) {
            val command = readlnOrNull()
            when (command) {
                "exit" -> {
                    gateway.stop()
                    break
                }
            }
        }
    }
    startKoin {
        modules(gatewayModule)
    }
    gateway.start()
    thread.join()
}