package utils.state

import CommandResult
import Frame
import FrameType

/**
 * Authentication state: register and login
 */
class AuthState : InteractionState(CommandsState()) {
    override fun start() {
        while (isActive) {
            userManager.writeLine("Для авторизации используйте команду login или register")
            when (val command = userManager.readLine().lowercase()) {
                "login", "register" -> {
                    if (interact(command)) {
                        exitState()
                    }
                }

                "exit" -> setState(ExitState())
            }
        }
    }

    /**
     * Read login and password
     *
     * @param command is the type of authentication
     * @return true if authentication was successful
     */
    private fun interact(command: String): Boolean {
        val login = readLogin()
        val password = readPassword()

        val frame = Frame(FrameType.AUTHORIZE_REQUEST)
        frame.setValue("login", login)
        frame.setValue("password", password)
        frame.setValue("type", command)

        val response = sendFrame(frame)
        when (response.type) {
            FrameType.AUTHORIZE_RESPONSE -> {
                when (val result = response.body["data"] as CommandResult) {
                    is CommandResult.Failure -> userManager.writeLine("Авторизация не пройдена: ${result.throwable.message}")
                    is CommandResult.Success -> {
                        interactor.setToken(result.message as String)
                        return true
                    }
                }
            }

            FrameType.ERROR -> userManager.writeLine("Авторизация не пройдена: ${response.body["error"]}")
            else -> userManager.writeLine("Сервер вернул что-то не то")
        }
        return false
    }

    /**
     * Reads login
     *
     * @return password
     */
    private fun readLogin(): String {
        while (true) {
            userManager.write("Введите логин: ")
            val login = userManager.readLine()
            if (login.length > 50) {
                userManager.writeLine("Логин должен быть меньше 50 символов")
                continue
            }
            return login
        }
    }

    /**
     * Reads password. Hide letters but only if there is available console
     *
     * @return password
     */
    private fun readPassword(): String {
        while (true) {
            userManager.write("Введите пароль: ")
            val password = userManager.readPassword()
            if (password.isEmpty()) {
                userManager.writeLine("Пароль должен быть не пустой строкой")
                continue
            }
            return password
        }
    }
}