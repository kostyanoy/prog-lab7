import di.serverModule
import org.koin.core.context.GlobalContext.startKoin
import org.apache.log4j.Logger

fun main(args: Array<String>) {
    val logger = Logger.getLogger(ServerApp::class.java)
    var serverPort = 2229
    if (args.isNotEmpty()) {
        serverPort = args[0].toIntOrNull() ?: serverPort
    }
    startKoin {
        modules(serverModule)
    }
    val server = ServerApp("localhost", serverPort)
    while (true) {
        print("connect or exit: ")
        when (readlnOrNull()) {
            "connect" -> {
                server.start()
                logger.info { "Сервер закрылся" }
            }

            "exit" -> {
                server.stop()
                break
            }

            "save" -> {
                server.saveCollection()
            }

            "load" -> {
                server.loadCollection()
            }
        }

    }
}


