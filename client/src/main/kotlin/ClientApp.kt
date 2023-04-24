import mu.KotlinLogging
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import serialize.FrameSerializer
import utils.Interactor
import java.net.ConnectException
import java.net.InetSocketAddress
import java.net.SocketTimeoutException
import java.nio.channels.SocketChannel

/**
 * Class of the application, uses the DI to get parameters
 */
class ClientApp(private val serverAddress: String, private val serverPort: Int) : KoinComponent {
    private val interactor by inject<Interactor>()
    private val frameSerializer by inject<FrameSerializer>()
    private val logger = KotlinLogging.logger {}

    private lateinit var channel: SocketChannel

    /**
     * Connects to the server
     */
    fun start() {
        try {
            channel = SocketChannel.open()
            channel.socket().connect(InetSocketAddress(serverAddress, serverPort), 5000)
            channel.socket()?.soTimeout = 5000 // timeout for server respond

            logger.info { "Произошло подключение к ${channel.remoteAddress}" }

            interactor.start(this)

            stop()
        } catch (e: SocketTimeoutException) {
            logger.info { "Сервер не отвечает (${e.message})" }
        } catch (e: ConnectException) {
            logger.info { "Невозможно подключиться (${e.message})" }
        }
    }

    /**
     * Closes the connection
     */
    fun stop() {
        if (channel.isOpen) {
            channel.close()
            logger.info { "Канал закрыт" }
        }
    }

    /**
     * Sends frame to the server
     *
     * @param frame which should be sent
     */
    fun sendFrame(frame: Frame) {
        val s = frameSerializer.serialize(frame)
        channel.socket().getOutputStream().write(s.toByteArray())
        logger.info { "Отправлен запрос на сервер ${frame.type}" }
    }

    /**
     * Receives frame to the server
     *
     * @return [Frame] which server sent
     */
    fun receiveFrame(): Frame {
        val array = ArrayList<Byte>()
        var char = channel.socket().getInputStream().read()
        while (Char(char) != '\n') {
            array.add(char.toByte())
            char = channel.socket().getInputStream().read()
        }
        val str = String(array.toByteArray())
        val frame = frameSerializer.deserialize(str)
        logger.info { "Получен ответ от сервера ${frame.type}" }
        return frame
    }
}

