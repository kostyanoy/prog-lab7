import data.MusicBand
import org.apache.log4j.Logger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import serialize.FrameSerializer
import utils.CommandManager
import utils.Saver
import utils.Storage
import utils.auth.token.Token
import utils.auth.token.TokenManager
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel

/**

The ServerApp class represents the server application that listens to incoming client requests,executes them and sends back the response.

@property [running] A boolean value indicating whether the server is running or not.
@property [selector] The Selector instance used for selecting incoming channels and operations.
@property [serverChannel] The ServerSocketChannel instance used to listen for incoming requests.
 */
class ServerApp(
    private val port: Int,
) : KoinComponent {
    private val commandManager: CommandManager by inject()
    private val tokenManager: TokenManager by inject()
    private val saver: Saver<LinkedHashMap<Int, MusicBand>> by inject()
    private val storage: Storage<LinkedHashMap<Int, MusicBand>, Int, MusicBand> by inject()
    private val serializer = FrameSerializer()
    private val logger = Logger.getLogger(ServerApp::class.java)
    var running = true
    private var selector: Selector = Selector.open()
    private lateinit var serverChannel: ServerSocketChannel
    lateinit var onConnect: (SelectionKey, Selector) -> Unit
    lateinit var onDisconnect: (SelectionKey, SocketChannel) -> Unit

    /**
    Starts the server and listens for incoming client requests.
     */
    fun start() {
        val serverChannel = ServerSocketChannel.open()
        serverChannel.bind(InetSocketAddress(port))
        serverChannel.configureBlocking(false)
        logger.info("Сервер запускается на порту: $port")
        serverChannel.register(selector, SelectionKey.OP_ACCEPT)

        while (running) {
            selector.select()
            val selectedKeys = selector.selectedKeys().iterator()

            while (selectedKeys.hasNext()) {
                val key = selectedKeys.next()
                selectedKeys.remove()

                if (!key.isValid) {
                    continue
                }

                if (key.isAcceptable) {
                    onConnect(key, selector)
                    //acceptConnection(key, selector)
                } else if (key.isReadable) {
                    readRequest(key)
                }
            }
        }
        serverChannel.close()
        selector.close()
        logger.info("Сервер закрыт")
    }

    /**
    Accepts a new incoming connection and registers it with the selector.

    @param [key] The SelectionKey of the incoming connection.
    @param [selector] The Selector instance used for selecting incoming channels and operations.
     */
    fun acceptConnection(key: SelectionKey, selector: Selector) {
        val serverSocketChannel = key.channel() as ServerSocketChannel
        val socketChannel = serverSocketChannel.accept()
        socketChannel.configureBlocking(false)
        socketChannel.register(selector, SelectionKey.OP_READ)
    }

    /**
    Reads the incoming request from the client, executes it and sends back the response.

    @param [key] The SelectionKey of the incoming request.
    @param [selector] The Selector instance used for selecting incoming channels and operations.
     */
    private fun readRequest(key: SelectionKey) {
        val socketChannel = key.channel() as SocketChannel
        val buffer = ByteBuffer.allocate(1024)

        try {
            socketChannel.read(buffer)
            buffer.flip()
            val len = buffer.limit() - buffer.position()
            val str = ByteArray(len)
            buffer.get(str, buffer.position(), len)
            buffer.flip()
            val request = serializer.deserialize(str.decodeToString())
            if (request.type == FrameType.EXIT){
                onDisconnect(key, socketChannel)
                return
            }
            val response = clientRequest(request)
            buffer.clear()
            buffer.put(serializer.serialize(response).toByteArray())
            buffer.put('\n'.code.toByte())
            buffer.flip()
            socketChannel.write(buffer)
        } catch (e: Exception) {
            logger.error(e.message)
            onDisconnect(key, socketChannel)
//            key.cancel()
//            socketChannel.close()
        }
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
                    request.body["name"] as String,
                    request.body["args"] as Array<Any>,
                    request.body["token"] as String
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
        val content = tokenManager.getContent(Token.parse(token))
        return try {
            command.execute(args + content)
        } catch (e: Exception) {
            CommandResult.Failure(commandName, e)
        }
    }

    /**
    Stops the server
     */
    fun stop() {
        running = false
        selector.wakeup()
    }

    fun saveCollection() {
        val saver: Saver<LinkedHashMap<Int, MusicBand>> by inject()
        saver.save(storage.getCollection { true })
        logger.info("Коллекция сохранена")
    }

    fun loadCollection() {
        saver.load().forEach { storage.insert(1, it.key, it.value) }
        logger.info("Коллекция загружена")
    }
}