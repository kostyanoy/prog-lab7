import di.clientModule
import mu.KotlinLogging
import org.koin.core.context.GlobalContext.startKoin


/**
 * Main function that starts the application
 */
fun main() {
    startKoin {
        modules(clientModule)
    }

    val logger = KotlinLogging.logger {}
    // this should make reconnect to the server possible
    // connect - tries to reconnect
    // exit    - stops the application
    var command = "connect"

    while (command != "exit") {
        if (command == "connect") {
            ClientApp("localhost", 2228).start()
            logger.info {"Клиент закрылся"}
        }
        print("connect or exit: ")
        command = readln()
    }
}

