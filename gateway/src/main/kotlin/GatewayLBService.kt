import mu.KotlinLogging
import org.koin.core.component.KoinComponent
import serialize.FrameSerializer
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.util.concurrent.TimeoutException

class GatewayLBService(
    private val clientPort: Int,
    private val serverPort: Int
) : KoinComponent {
    var running = true
    private val logger = KotlinLogging.logger {}
    private val serializer = FrameSerializer()
    private val clientSelector = Selector.open()
    private val serverSelector = Selector.open()
    private val clientServerSocketChannel = ServerSocketChannel.open()
    private val serverServerSocketChannel = ServerSocketChannel.open()
    private val servers = mutableListOf<SocketChannel>()
    private var currentServerIndex = 0
    init {
        clientServerSocketChannel.socket().bind(InetSocketAddress(clientPort))
        clientServerSocketChannel.configureBlocking(false)
        clientServerSocketChannel.register(clientSelector, SelectionKey.OP_ACCEPT)

        serverServerSocketChannel.socket().bind(InetSocketAddress(serverPort))
        serverServerSocketChannel.configureBlocking(false)
        serverServerSocketChannel.register(serverSelector, SelectionKey.OP_ACCEPT)
    }

    fun start() {
        logger.info("GatewayLBService стартует")
        while (running) {
            if (clientSelector.selectNow() > 0) {
                val keys = clientSelector.selectedKeys()
                val iterator = keys.iterator()
                while (iterator.hasNext()) {
                    val key = iterator.next()
                    if (key.isAcceptable) {
                        connectToClient(key)
                    } else if (key.isReadable) {
                        handleClientRequest(key)
                    }
                    iterator.remove()
                }
            }
            if (serverSelector.selectNow() > 0) {
                val keys = serverSelector.selectedKeys()
                val iterator = keys.iterator()
                while (iterator.hasNext()) {
                    val key = iterator.next()
                    if (key.isAcceptable) {
                        connectToServer(key)
                    } else if (key.isReadable) {
                        handleServerRequest(key)
                    }
                    iterator.remove()
                }
            }
        }
    }

    fun stop() {
        running = false
        clientSelector.wakeup()
        serverSelector.wakeup()
    }

    private fun connectToClient(key: SelectionKey) {
        val clientChannel = clientServerSocketChannel.accept()
        clientChannel.configureBlocking(false)
        clientChannel.register(clientSelector, SelectionKey.OP_READ, SelectionKey.OP_WRITE)
        logger.info { "Подключился клиент: ${clientChannel.remoteAddress}" }
    }

    private fun connectToServer(key: SelectionKey) {
        val serverChannel = serverServerSocketChannel.accept()
        serverChannel.configureBlocking(false)
        serverChannel.register(
            serverSelector,
            SelectionKey.OP_READ,
            SelectionKey.OP_WRITE
        )
        servers.add(serverChannel)
        logger.info { "Подключился сервер: ${serverChannel.remoteAddress}. Доступно серверов: ${servers.count()}" }
    }

    private fun handleClientRequest(key: SelectionKey) {
        val clientChannel = key.channel() as SocketChannel
        try {
            val request = receiveRequest(clientChannel)
            if (request.type == FrameType.EXIT) {
                logger.info { "Отключен клиент ${clientChannel.remoteAddress}" }
                clientChannel.close()
                return
            }
            val response = routeRequest(request)
            sendResponse(clientChannel, response)
        } catch (e: Exception) {
            logger.error("Ошибка обработки запроса от клиента", e)
            clientChannel.close()
        }
    }

    private fun handleServerRequest(key: SelectionKey) {
        val serverChannel = key.channel() as SocketChannel
        try {
            val request = receiveRequest(serverChannel)
            if (request.type == FrameType.EXIT) {
                servers.remove(serverChannel)
                logger.info { "Отключен сервер ${serverChannel.remoteAddress}" }
                serverChannel.close()
                logger.info { "Доступно серверов: ${servers.count()}" }
            }
        } catch (e: Exception) {
            logger.error("Показалось", e)
        }
    }

    private fun receiveRequest(channel: SocketChannel): Frame {
        val buffer = ByteBuffer.allocate(1024)
        var tries = 0
        var num = channel.read(buffer)
        while (num == 0) {
            Thread.sleep(200)
            num = channel.read(buffer)
            tries++
            if (tries > 5)
                throw TimeoutException("Нечего читать =(")
        }
        buffer.flip()
        val len = buffer.limit() - buffer.position()
        val str = ByteArray(len)
        buffer.get(str, buffer.position(), len)
        val request = serializer.deserialize(str.decodeToString())
        logger.info { "Получен Frame: ${request.type}" }
        return request
    }

    private fun routeRequest(request: Frame): Frame {
        logger.info { "Доступно серверов: ${servers.count()}" }
        if (servers.isEmpty()) {
            throw IllegalStateException("Нет доступных серверов")
        }
        val server = servers[nextIndex()]
        val buffer = ByteBuffer.wrap((serializer.serialize(request) + "\n").toByteArray())
        server.write(buffer)
        logger.info { "Маршрутизирован Frame к ${server.remoteAddress}" }
        return receiveRequest(server)
    }

    private fun sendResponse(clientChannel: SocketChannel, response: Frame) {
        val buffer = ByteBuffer.wrap((serializer.serialize(response) + '\n').toByteArray())
        clientChannel.write(buffer)// отправляем данные в канал клиента
        logger.info { "Отправлен ответ на клиент ${clientChannel.remoteAddress}" }
    }

    private fun nextIndex(): Int {
        if (servers.isEmpty())
            currentServerIndex = 0
        else
            currentServerIndex = (currentServerIndex + 1) % servers.count()
        return currentServerIndex
    }
}