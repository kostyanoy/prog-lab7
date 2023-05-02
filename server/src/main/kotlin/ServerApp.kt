import data.MusicBand
import org.apache.log4j.Logger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import serialize.FrameSerializer
import utils.CommandManager
import utils.Saver
import utils.Storage
import java.net.ConnectException
import java.net.InetSocketAddress
import java.net.SocketTimeoutException
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import java.util.concurrent.Executors
import java.util.concurrent.locks.ReentrantReadWriteLock

class ServerApp(
    private val gatewayAddress: String,
    private val gatewayPort: Int
) : KoinComponent {
    private val commandManager by inject<CommandManager>()
    private val frameSerializer by inject<FrameSerializer>()
    private val saver: Saver<LinkedHashMap<Int, MusicBand>> by inject()
    private val storage: Storage<LinkedHashMap<Int, MusicBand>, Int, MusicBand> by inject()
    private val serializer = FrameSerializer()
    private val logger = Logger.getLogger(ServerApp::class.java)
    private lateinit var channel: SocketChannel
    val executor = Executors.newFixedThreadPool(10) // создаем пул потоков
    val lock = ReentrantReadWriteLock() //синхронизации доступа к коллекции
    //1 блок
    //подключается к GatewayLBService как клиент
    fun start() {
        try {
            channel = SocketChannel.open()
            channel.socket().connect(InetSocketAddress(gatewayAddress, gatewayPort), 5000)
            logger.info { "Подключено к  GatewayLBService: $gatewayAddress:$gatewayPort" }
        } catch (e: SocketTimeoutException) {
            logger.info { "GatewayLBService не отвечает (${e.message})" }
        } catch (e: ConnectException) {
            logger.info { "Не удается подключиться к GatewayLBService (${e.message})" }
        }
    }

    fun stop() {
        if (channel.isOpen) {
            channel.close()
            logger.info { "Канал закрыт" }
        }
    }
    //1 блок
    // отправляем запрос GatewayLBService
    private fun sendRequest(request: Frame): Frame {
        val buffer = ByteBuffer.allocate(1024)
        buffer.put(serializer.serialize(request).toByteArray())
        buffer.put('\n'.code.toByte())
        buffer.flip()
        channel.write(buffer)
        buffer.clear()
        channel.read(buffer)// читаем ответ от GatewayLBService
        buffer.flip()
        val len = buffer.limit() - buffer.position()
        val str = ByteArray(len)
        buffer.get(str, buffer.position(), len)
        return serializer.deserialize(str.decodeToString())
    }
//читаем ответ от GatewayLBService
    fun receiveFromGatewayLBService(): Frame {
        val array = ArrayList<Byte>()
        var char = channel.socket().getInputStream().read().toChar()
        while (char != '\n') {
            array.add(char.toByte())
            char = channel.socket().getInputStream().read().toChar()
        }
        val str = String(array.toByteArray())
        val frame = frameSerializer.deserialize(str)
        logger.info { "Получен ответ от GatewayLBService ${frame.type}" }
        return frame
    }

    //3 блок
    //ответ сервера
    private fun serverRequest(request: Frame): Frame {
        return when (request.type) {
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
    }
    //кидает ответ глбс
    private fun sendResponse(socketChannel: SocketChannel, response: Frame) {
        val buffer = ByteBuffer.allocate(1024)
        buffer.put(serializer.serialize(response).toByteArray())
        buffer.put('\n'.code.toByte())
        buffer.flip()
        socketChannel.write(buffer)
        buffer.clear()
    }
    //3 блок


    //неизменяемый блок
    fun saveCollection() {
        val saver: Saver<LinkedHashMap<Int, MusicBand>> by inject()
        saver.save(storage.getCollection { true })
        logger.info("Коллекция сохранена")
    }

    fun loadCollection() {
        val saver: Saver<LinkedHashMap<Int, MusicBand>> by inject()
        saver.load().forEach { storage.insert(it.key, it.value) }
        logger.info("Коллекция загружена")
    }
    //неизменяемый блок
}