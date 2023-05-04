import mu.KotlinLogging
import org.koin.core.component.KoinComponent
import serialize.FrameSerializer
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.util.concurrent.Executors
import java.util.concurrent.TimeoutException
import java.util.concurrent.locks.ReentrantReadWriteLock

class GatewayLBService(
    private val clientPort: Int,
    private val serverPort: Int
) : KoinComponent {
    private val logger = KotlinLogging.logger {}
    private val serializer = FrameSerializer()
    private val clientSelector = Selector.open()
    private val serverSelector = Selector.open()
    private val clientServerSocketChannel = ServerSocketChannel.open()
    private val serverServerSocketChannel = ServerSocketChannel.open()
    private val servers = mutableListOf<SocketChannel>()
    private val executor = Executors.newFixedThreadPool(10)
    private val responseExecutor = Executors.newCachedThreadPool()
    private val lock = ReentrantReadWriteLock()
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
        while (true) {
            if (clientSelector.selectNow() > 0) {
                val keys = clientSelector.selectedKeys()
                val iterator = keys.iterator()
                while (iterator.hasNext()) {
                    val key = iterator.next()
                    if (key.isAcceptable) {
//                        executor.execute {
                            logger.info{"Поток обработал запрос клиента"}
                            connectToClient(key)
//                        }
                    } else if (key.isReadable) {
//                        executor.execute {
                            logger.info{"Поток обработал запрос клиента"}
                            handleClientRequest(key)
//                        }
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
//                        executor.execute {
                            logger.info{"Поток обработал запрос сервера"}
                            connectToServer(key)
//                        }
                    } else if (key.isReadable) {
//                        executor.execute {
                            logger.info{"Поток обработал запрос сервера"}
                            handleServerRequest(key)
//                        }
                    }
                    iterator.remove()
                }
            }
        }
    }


    fun stop() {
        logger.info{"Остановка GatewayLBService..."}
//        executor.shutdownNow()
//        responseExecutor.shutdownNow()
        clientSelector.wakeup()
        serverSelector.wakeup()
        logger.info{"GatewayLBService остановлен"}
    }


    private fun connectToClient(key: SelectionKey) {
        try {
            val clientChannel = clientServerSocketChannel.accept()
            clientChannel.configureBlocking(false)
            clientChannel.register(clientSelector, SelectionKey.OP_READ, SelectionKey.OP_WRITE)
            logger.info { "Подключился клиент: ${clientChannel.remoteAddress}" }
        } catch (e: IOException) {
            logger.error("Ошибка при подключении клиента", e)
        }
    }

    private fun connectToServer(key: SelectionKey) {
        try {
            val serverChannel = serverServerSocketChannel.accept()
            serverChannel.configureBlocking(false)
            val serverSelector = Selector.open()
            serverChannel.register(serverSelector, SelectionKey.OP_READ, SelectionKey.OP_WRITE)
            lock.writeLock().lock()
            servers.add(serverChannel)
            lock.writeLock().unlock()
            logger.info { "Подключился сервер: ${serverChannel.remoteAddress}. Доступно серверов: ${servers.count()}" }
        } catch (e: IOException) {
            logger.error("Ошибка при подключении сервера", e)
        }
    }



    private fun handleClientRequest(key: SelectionKey) {
        val clientChannel = key.channel() as SocketChannel
        try {
            val request = receiveRequest(clientChannel)
            val response = routeRequest(request)
            sendResponse(clientChannel, response)
            if (request.type == FrameType.EXIT) {
                logger.info { "Отключен клиент ${clientChannel.remoteAddress}" }
                clientChannel.close()
            }
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
        clientChannel.write(buffer)
        logger.info { "Отправлен ответ на клиент ${clientChannel.remoteAddress}" }
    }

    private fun nextIndex(): Int {
        currentServerIndex = if (servers.isEmpty())
            0
        else
            (currentServerIndex + 1) % servers.count()
        return currentServerIndex
    }
}