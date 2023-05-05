package utils.state

import Frame
import FrameType
import org.koin.core.component.inject
import utils.CommandManager

/**
 * State for requesting commands from server
 */
class UpdateState : InteractionState(CommandsState()) {
    private val commandManager: CommandManager by inject()


    override fun start() {
        val frame = Frame(FrameType.LIST_OF_COMMANDS_REQUEST)
        frame.setValue("token", interactor.getToken())
        val response = sendFrame(frame)

        when(response.type){
            FrameType.LIST_OF_COMMANDS_RESPONSE -> update(response)
            FrameType.ERROR -> userManager.writeLine("На сервере что-то пошло не так")
            else -> userManager.writeLine("Сервер вернул что-то не то")
        }
        exitState()
    }

    /**
     * Gives a response to the CommandManager
     */
    private fun update(frame: Frame) {
        if (commandManager.updateCommands(frame))
            userManager.writeLine("Команды обновлены")
        else
            userManager.writeLine("Не удалось обновить команды")
    }
}