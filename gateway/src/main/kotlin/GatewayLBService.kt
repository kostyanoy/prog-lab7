import org.koin.core.component.KoinComponent
import serialize.FrameSerializer
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import org.apache.log4j.Logger

class GatewayLBService(
    private val clientPort: Int,
    private val serverPort: Int
) : KoinComponent {
    private val logger = Logger.getLogger(GatewayLBService::class.java)
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
        logger.info("Подключается GatewayLBService")
        while (true) {
            //клиенты
            if (clientSelector.selectNow() > 0) { // проверяется, есть ли доступные клиенты для подключения
                val keys = clientSelector.selectedKeys() // получаем ключи, соответствующие доступным клиентам
                val iterator = keys.iterator()
                while (iterator.hasNext()) {
                    val key = iterator.next()
                    if (key.isAcceptable) {
                        connectToClient(key)
                    } else if (key.isReadable) {
                        val clientChannel = key.channel() as SocketChannel // получаем канал клиента
                        try {
                            val request = receiveRequest(clientChannel) // получаем запрос от клиента
                            val response = routeRequest(request) // маршрутизируем запрос
                            sendResponse(clientChannel, response) // отправляем ответ клиенту
                        } catch (e: Exception) {
                            logger.error("Ошибка обработки запроса от клиента", e)
                        } finally {
                            clientChannel.close()
                        }
                    }
                    iterator.remove() // удаляем обработанный ключ из множества
                }
            }
            //серверы
            if (serverSelector.selectNow() > 0) { // проверяем, есть ли доступные серверы для подключения
                val keys = serverSelector.selectedKeys()
                val iterator = keys.iterator()
                while (iterator.hasNext()) {
                    val key = iterator.next()
                    if (key.isAcceptable) {
                        connectToServer(key)
                    } else if (key.isReadable) {
                        readServerRequest(key, clientSelector, serverSelector) // читаем запрос от сервера
                    }
                    iterator.remove() // удаляем обработанный ключ из множества
                }
            }

        }
    }
    // подлючение к клиенту
    private fun connectToClient(key: SelectionKey) {
        val serverChannel = key.channel() as ServerSocketChannel
        val clientChannel = serverChannel.accept()
        clientChannel.configureBlocking(false)
        clientChannel.register(clientSelector, SelectionKey.OP_READ)
    }
    // подлючение к серверу
    private fun connectToServer(key: SelectionKey) {
        val serverChannel = SocketChannel.open()
        serverChannel.configureBlocking(false)
        serverChannel.connect(InetSocketAddress("localhost", serverPort))
        serverChannel.register(serverSelector, SelectionKey.OP_CONNECT)
        servers.add(serverChannel) // добавляем канал сервера в список доступных серверов
    }
    // чтение запроса от клиента
    private fun receiveRequest(channel: SocketChannel): ByteArray {
        val buffer = ByteBuffer.allocate(1024)
        channel.read(buffer)
        buffer.flip()
        val len = buffer.limit() - buffer.position()
        val bytes = ByteArray(len)
        buffer.get(bytes, buffer.position(), len)
        return bytes
    }
    // чтение запросов от сервера
    private fun readServerRequest(key: SelectionKey, clientSelector: Selector, serverSelector: Selector) {
        val clientChannel = key.attachment() as SocketChannel
        val serverChannel = key.channel() as SocketChannel
        try {
            val request = receiveRequest(serverChannel)// читаем запрос от сервера
            val response = routeRequest(request) // обрабатываем запрос и получаем ответ
            sendResponse(clientChannel, response)// отправляем ответ клиенту
            key.interestOps(SelectionKey.OP_WRITE)
        } catch (e: Exception) {
            logger.error("Ошибка обработки запроса от сервера", e)
            clientChannel.close()
            serverChannel.close()
        }
    }
    // маршутизируем запросы к серверу
    private fun routeRequest(request: ByteArray): ByteArray {
        if (servers.isEmpty()) {
            throw IllegalStateException("Нет доступных серверов")
        }
        val server = servers[currentServerIndex]
        currentServerIndex = (currentServerIndex + 1) % servers.size // round-robin
        server.write(ByteBuffer.wrap(request))// отправляем запрос на сервер
        return receiveRequest(server)// получаем ответ от сервера
    }
// отправка ответов клиенту
    private fun sendResponse(clientChannel: SocketChannel, response: ByteArray) {
        val buffer = ByteBuffer.wrap(response)
        while (buffer.hasRemaining()) {// пока в буфере есть данные
            clientChannel.write(buffer)// отправляем данные в канал клиента
        }
    }
}