package utils.state

/**
 * State for command execution
 */
class CommandsState : InteractionState() {
    private val invitation = ">>>"

    override fun start() {
        interactor.interact("update_commands")
        userManager.writeLine("Здрасьте, для вывода списка команд введите help")
        while (isActive) {
            userManager.write(invitation)
            when (val text = userManager.readLine()) {
                "exit" -> setState(ExitState())
                //"update_commands" -> setState(UpdateState())
                else -> interactor.interact(text)
            }
        }
    }
}