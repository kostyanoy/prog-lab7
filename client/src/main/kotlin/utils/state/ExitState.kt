package utils.state

import Frame

/**
 * State for stopping the client
 */
class ExitState : InteractionState() {
    override fun start() {
        interactor.getClient().sendFrame(Frame(FrameType.EXIT))
        exitState()
    }

    override fun exitState() {
        stop()
    }
}