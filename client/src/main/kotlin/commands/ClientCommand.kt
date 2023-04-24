package commands

import Command
import org.koin.core.component.inject
import utils.Interactor

/**
 * Abstract class for client commands which is not sent to the server
 */
abstract class ClientCommand : Command() {
    val interactor: Interactor by inject()
}