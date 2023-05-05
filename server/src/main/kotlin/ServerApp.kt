import mu.KotlinLogging
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import serialize.Serializer
import utils.CommandManager
import java.io.IOException
import java.net.ConnectException
import utils.auth.UserStatus
import utils.auth.token.Content
import utils.auth.token.Token
import utils.auth.token.Tokenizer
import utils.database.Database
import java.io.PrintWriter
import java.io.StringWriter
import java.net.InetSocketAddress
import java.net.SocketTimeoutException
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import kotlin.concurrent.read
import kotlin.math.min

/**
 * The ServerApp class represents the server application that listens to incoming client requests,executes them and sends back the response.
 */
class ServerApp(
    private val gatewayAddress: String,
    private val gatewayPort: Int
) : KoinComponent {
    private val commandManager by inject<CommandManager>()
    private val frameSerializer by inject<Serializer<Frame>>()
    private var isActive = true
    private val tokenManager by inject<Tokenizer>()

    private val logger = KotlinLogging.logger {}
    private lateinit var channel: SocketChannel

    /**
     * Starts the server and listens for incoming client requests.
     */
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

    /**
    Stops the server
     */
    fun stop() {
        if (channel.isOpen) {
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
    /**
    Processes a client request and returns a response frame.

    @param [request] the request frame received from the client
    @return the response frame to be sent back to the client
     */
    private fun clientRequest(request: Frame): Frame {
        when (request.type) {
            FrameType.COMMAND_REQUEST -> {
                val response = Frame(FrameType.COMMAND_RESPONSE)
                val result = execute(
                    request.body["name"] as String, request.body["args"] as Array<Any>, request.body["token"] as String
                )
                response.setValue("data", result)
                return response
            }

            FrameType.LIST_OF_COMMANDS_REQUEST -> {
                val response = Frame(FrameType.LIST_OF_COMMANDS_RESPONSE)
                val commands = commandManager.commands.mapValues { it.value.getArgumentTypes() }.toMap()
                response.setValue("commands", commands)
                return response
            }

            FrameType.AUTHORIZE_REQUEST -> {
                val response = Frame(FrameType.AUTHORIZE_RESPONSE)
                val result = execute(
                    request.body["type"] as String,
                    arrayOf(request.body["login"] as String, request.body["password"] as String),
                    ""
                )
                response.setValue("data", result)
                return response
            }

            else -> {
                val response = Frame(FrameType.ERROR)
                response.setValue("error", "Неверный тип запроса")
                return response
            }
        }
    }

    private fun execute(commandName: String, args: Array<Any>, token: String): CommandResult {
        val command = commandManager.getCommand(commandName)
        val content = if (token.isBlank()) Content(-1, UserStatus.USER) else tokenManager.getContent(Token.parse(token))
        return try {
            command.execute(args + content)
        } catch (e: Throwable) {
            val sw = StringWriter()
            val pw = PrintWriter(sw)
            e.printStackTrace(pw)
            logger.error(e.message + sw.toString())
            CommandResult.Failure(commandName, e)
        }
    }

    fun updateTables() {
        val database: Database by inject()
        database.updateTables()
}