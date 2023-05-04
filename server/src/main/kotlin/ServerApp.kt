import mu.KotlinLogging
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import serialize.Serializer
import utils.CommandManager
import java.io.IOException
import java.net.ConnectException
import java.net.InetSocketAddress
import java.net.SocketTimeoutException
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import kotlin.concurrent.read

class ServerApp(
    private val gatewayAddress: String,
    private val gatewayPort: Int
) : KoinComponent {
    private val commandManager by inject<CommandManager>()
    private val frameSerializer by inject<Serializer<Frame>>()
    private var isActive = true

    private val logger = KotlinLogging.logger {}
    private lateinit var channel: SocketChannel

    fun start() {
        try {
            channel = SocketChannel.open()
            channel.socket().connect(InetSocketAddress(gatewayAddress, gatewayPort), 5000)
            logger.info { "Подключено к  GatewayLBService: $gatewayAddress:$gatewayPort" }
            while (isActive) {
                try {
                    val request = receiveFromGatewayLBService()
                    val response = serverRequest(request)
                    sendResponse(response)
                } catch (e: IOException) {
                    logger.error { e }
                    channel.close()
                    isActive = false
                }

            }
        } catch (e: SocketTimeoutException) {
            logger.info { "GatewayLBService не отвечает (${e.message})" }
        } catch (e: ConnectException) {
            logger.info { "Не удается подключиться к GatewayLBService (${e.message})" }
        }
    }

    fun stop() {
        if (channel.isOpen) {
            //sendResponse(Frame(FrameType.EXIT))
            channel.close()
            logger.info { "Канал закрыт" }
        }
    }

    private fun receiveFromGatewayLBService(): Frame {
        val array = ArrayList<Byte>()
        logger.info { "Ожидаем запроса..." }
        var char = channel.socket().getInputStream().read()
        while (char.toChar() != '\n') {
            array.add(char.toByte())
            char = channel.socket().getInputStream().read()
        }
        val str = String(array.toByteArray())
        val frame = frameSerializer.deserialize(str)
        logger.info { "Получен ответ от GatewayLBService ${frame.type}" }
        return frame
    }
    private fun serverRequest(request: Frame): Frame {
        return try {
            when (request.type) {
                FrameType.COMMAND_REQUEST -> {
                    val response = Frame(FrameType.COMMAND_RESPONSE)
                    val commandName = request.body["name"] as String
                    val args = request.body["args"] as Array<Any>
                    val command = commandManager.getCommand(commandName)
                    val result = command.execute(args)
                    response.setValue("data", result)
                    response
                }
                FrameType.LIST_OF_COMMANDS_REQUEST -> {
                    val response = Frame(FrameType.LIST_OF_COMMANDS_RESPONSE)
                    val commands = commandManager.commands.mapValues { it.value.getArgumentTypes() }.toMap()
                    response.setValue("commands", commands)
                    response
                }
                else -> {
                    val response = Frame(FrameType.COMMAND_RESPONSE)
                    response.setValue("data", "Неверный тип запроса")
                    response
                }
            }
        } catch (e: Exception) {
            val response = Frame(FrameType.COMMAND_RESPONSE)
            response.setValue("data", "Произошла ошибка: ${e.message}")
            response
        }
    }


    private fun sendResponse(response: Frame) {
        val buffer = ByteBuffer.allocate(1024)
        buffer.put(frameSerializer.serialize(response).toByteArray())
        buffer.put('\n'.code.toByte())
        buffer.flip()
        channel.write(buffer)
        buffer.clear()
        logger.info { "Отправлен Frame ${response.type}" }
    }
}