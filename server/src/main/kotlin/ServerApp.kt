import mu.KotlinLogging
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import serialize.Serializer
import utils.CommandManager
import utils.auth.UserStatus
import utils.auth.token.Content
import utils.auth.token.Token
import utils.auth.token.Tokenizer
import utils.database.Database
import utils.database.tables.Bands
import utils.database.tables.Users
import utils.database.updateTables
import java.io.IOException
import java.io.PrintWriter
import java.io.StringWriter
import java.net.ConnectException
import java.net.InetSocketAddress
import java.net.SocketTimeoutException
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import java.util.concurrent.BlockingQueue
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.write

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
    private val executor = Executors.newFixedThreadPool(10)
    private val responseExecutor = Executors.newCachedThreadPool()
    private val lock = ReentrantReadWriteLock()
    private val requestQueue: BlockingQueue<Frame> = LinkedBlockingQueue()
    private val readLock = ReentrantReadWriteLock().readLock()

    /**
     * Starts the server and listens for incoming client requests.
     */
    fun start() {
        try {
            channel = SocketChannel.open()
            channel.socket().connect(InetSocketAddress(gatewayAddress, gatewayPort), 5000)
            logger.info { "Подключено к GatewayLBService: $gatewayAddress:$gatewayPort" }
            val receiveThread = Thread {
                while (isActive) {
                    try {
                        val request = receiveFromGatewayLBService()
                        requestQueue.offer(request)
                    } catch (e: IOException) {
                        logger.error { e }
                        channel.close()
                        isActive = false
                    }
                }
            }
            receiveThread.start()

            val processThread = Thread {
                while (isActive) {
                    val request = requestQueue.poll(1000, TimeUnit.MILLISECONDS) ?: continue
                    executor.execute {
                        val response = serverRequest(request)
                        responseExecutor.execute {
                            sendResponse(response, request.body["address"] as String?)
                        }
                    }
                }
            }
            processThread.start()

        } catch (e: SocketTimeoutException) {
            logger.info { "GatewayLBService не отвечает (${e.message})" }
        } catch (e: ConnectException) {
            logger.info { "Не удается подключиться к GatewayLBService (${e.message})" }
        }
    }

    /**
     * Stops the server.
     */
    fun stop() {
        isActive = false
        executor.shutdown()
        responseExecutor.shutdown()
        if (channel.isOpen) {
            val response = Frame(FrameType.EXIT)
            sendResponse(response, null)
            channel.close()
            val db: Database by inject()
            db.close()
            logger.info { "Канал закрыт" }
        }
    }

    /**
     * Receives a frame from the GatewayLBService.
     */
    private fun receiveFromGatewayLBService(): Frame {
        val array = ArrayList<Byte>()
        logger.info { "Ожидаем запроса..." }
        readLock.lock()
        try {
            var char = channel.socket().getInputStream().read()
            while (char.toChar() != '\n') {
                array.add(char.toByte())
                char = channel.socket().getInputStream().read()
            }
            val str = String(array.toByteArray())
            logger.info { str }
            val frame = frameSerializer.deserialize(str)
            logger.info { "Получен ответ от GatewayLBService ${frame.type}" }
            return frame
        } finally {
            readLock.unlock()
        }
    }

    /**
     * Processes a client request and returns a response frame.
     *
     * @param [request] the request frame received from the client
     * @return the response frame to be sent back to the client
     *
     */
    private fun serverRequest(request: Frame): Frame {
        try {
            when (request.type) {
                FrameType.COMMAND_REQUEST -> {
                    val response = Frame(FrameType.COMMAND_RESPONSE)
                    val result = execute(
                        request.body["name"] as String,
                        request.body["args"] as Array<Any>,
                        request.body["token"] as String
                    )
                    response.setValue("data", result)
                    request.body["address"]?.let { response.setValue("address", it) }
                    return response
                }

                FrameType.LIST_OF_COMMANDS_REQUEST -> {
                    val response = Frame(FrameType.LIST_OF_COMMANDS_RESPONSE)
                    val commands = commandManager.commands.mapValues { it.value.getArgumentTypes() }.toMap()
                    response.setValue("commands", commands)
                    request.body["address"]?.let { response.setValue("address", it) }
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
                    request.body["address"]?.let { response.setValue("address", it) }
                    return response
                }

                else -> {
                    val response = Frame(FrameType.ERROR)
                    response.setValue("error", "Неверный тип запроса")
                    return response
                }
            }
        } catch (e: Exception) {
            val response = Frame(FrameType.COMMAND_RESPONSE)
            response.setValue("data", "Произошла ошибка: ${e.message}")
            return response
        }
    }


    /**
     * Sends the provided [response] to the gateway.
     *
     * @param response [Frame] object to be sent.
     * @param address the address to include in the response frame.
     */
    private fun sendResponse(response: Frame, address: String?) {
        if (address != null) {
            response.setValue("address", address)
        }
        val serializedResponse = (frameSerializer.serialize(response) + "\n").toByteArray()
        val buffer = ByteBuffer.wrap(serializedResponse)
        try {
            lock.write {
                channel.write(buffer)
                logger.info { "Отправлен Frame ${response.type}" }
            }
        } catch (e: IOException) {
            logger.error(e) { "Ошибка при отправке ответа: ${e.message}" }
            channel.close()
            isActive = false
        }
    }

    /**
     * Executes a command with the specified name and arguments, and returns the result.
     * @param [token] the authorization token to use when executing the command.
     */
    private fun execute(commandName: String, args: Array<Any>, token: String): CommandResult {
        val command = commandManager.getCommand(commandName)
        val content = if (token.isBlank()) Content(-1, UserStatus.USER) else tokenManager.getContent(Token.parse(token))
        lock.writeLock().lock()
        try {
            return try {
                command.execute(args + content)
            } catch (e: Throwable) {
                val sw = StringWriter()
                val pw = PrintWriter(sw)
                e.printStackTrace(pw)
                logger.error(e.message + sw.toString())
                CommandResult.Failure(commandName, e)
            }
        } finally {
            lock.writeLock().unlock()
        }
    }

    /**
     * Updates database tables using [Database.updateTables].
     */
    fun updateTables() {
        val database: Database by inject()
        database.updateTables(Bands, Users)
    }
}