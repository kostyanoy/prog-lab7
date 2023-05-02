package commands

import ArgumentType
import Command
import CommandResult
import org.koin.core.component.inject
import utils.auth.AuthManager

/**
 * The command allows user to create new account.
 *
 * Fails if the login doesn't exist or password is incorrect.
 */
class Login : Command() {
    override fun execute(args: Array<Any>): CommandResult {
        val login = args[0] as String
        val password = args[1] as String

        val authManager: AuthManager by inject()
        val token = authManager.login(login, password)
        return CommandResult.Success("login", token.toString())
    }

    override fun getDescription(): String = "login : enter the account"

    override fun getArgumentTypes(): Array<ArgumentType> = arrayOf(ArgumentType.STRING, ArgumentType.STRING)
}