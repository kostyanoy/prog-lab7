package utils.state

import Frame
import mu.KotlinLogging
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import utils.Interactor
import utils.ReaderWriter

/**
 * States of InteractionManager
 *
 * @param nextState is the starting state. Can be changes then it will be next state
 */
abstract class InteractionState(private var nextState: InteractionState? = null) :
    KoinComponent {
    protected val interactor: Interactor by inject()
    protected val userManager: ReaderWriter by inject()
    protected val logger = KotlinLogging.logger {}

    protected var isActive = true

    /**
     * Start state logic
     */
    abstract fun start()

    /**
     * Stop state and change to next
     */
    open fun exitState() {
        isActive = false
        interactor.setState(nextState ?: AuthState())
    }

    /**
     * Stop state and InteractionManager
     */
    fun stop() {
        nextState = AuthState()
        interactor.exit()
        exitState()
    }

    /**
     * Set next state
     *
     * @param state the next state will be toggled
     */
    fun setState(state: InteractionState) {
        nextState = state
        exitState()
    }

    /**
     * Send and receive frame-response
     *
     * @param frame the frame to send
     * @return frame-response
     */
    fun sendFrame(frame: Frame): Frame = interactor.getClient().sendAndReceiveFrame(frame)
}