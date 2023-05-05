package commands

import ArgumentType
import Command
import CommandResult
import org.koin.core.component.inject
import utils.auth.AuthManager

/**
 * The command allows user to create new account.
 *
 * Fails if the login already exists.
 */
class Register : Command() {
    override fun execute(args: Array<Any>): CommandResult {
        val login = args[0] as String
        val password = args[1] as String

        val authManager: AuthManager by inject()
        val token = authManager.register(login, password)
        return CommandResult.Success("register", token.toString())
    }

    override fun getDescription(): String = "register : create new account"

    override fun getArgumentTypes(): Array<ArgumentType> = arrayOf(ArgumentType.STRING, ArgumentType.STRING)
}