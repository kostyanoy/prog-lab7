import di.serverModule
import org.koin.core.context.GlobalContext.startKoin
import utils.server
import kotlin.concurrent.thread


/**
 * Main function that starts the server application and listens to commands from the console.
 * The application can be stopped using the 'exit' command, and the collection can be saved or loaded.
 */
fun main(args: Array<String>) {
    var serverPort = 2228
    if (args.isNotEmpty()) {
        serverPort = args[0].toIntOrNull() ?: serverPort
    }


    val server = server {
        port = serverPort
        onConnect { key, selector ->
            run {
                println("Клиент!!!!!!")
                server.acceptConnection(key, selector)
            }
        }
        onDisconnect { key, channel ->
            run {
                key.cancel()
                channel.close()
            }
        }
//        timerAction(3000) {
//            onTimerOn {
//                println("sas")
//            }
//        }
    }
//    val server = ServerApp(port)


    val thread = thread {
        while (true) {
            val command = readlnOrNull()
            when (command) {
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
    startKoin {
        modules(serverModule)
    }
    server.start()
    thread.join()
}