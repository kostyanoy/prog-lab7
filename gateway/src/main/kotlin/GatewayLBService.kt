import mu.KotlinLogging
import org.koin.core.component.KoinComponent
import serialize.FrameSerializer
import java.io.IOException
import java.lang.StringBuilder
import java.net.InetSocketAddress
import java.net.SocketException
import java.nio.ByteBuffer
import java.nio.channels.CancelledKeyException
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.util.concurrent.Executors
import java.util.concurrent.TimeoutException
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * Gateway Load Balancer service.
 *
 * @property [clientPort] the port number for incoming client connections.
 * @property [serverPort] the port number for outgoing server connections.
 */
class GatewayLBService(
    clientPort: Int,
    serverPort: Int
) : KoinComponent {
    private var isRunning = true
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
    private var clientAddress: InetSocketAddress? = null

    @Volatile
    private var currentServerIndex = 0
    init {
        clientServerSocketChannel.socket().bind(InetSocketAddress(clientPort))
        clientServerSocketChannel.configureBlocking(false)
        clientServerSocketChannel.register(clientSelector, SelectionKey.OP_ACCEPT)

        serverServerSocketChannel.socket().bind(InetSocketAddress(serverPort))
        serverServerSocketChannel.configureBlocking(false)
        serverServerSocketChannel.register(serverSelector, SelectionKey.OP_ACCEPT)
    }
    /**
     * Locks the selection key and executes the given action.
     *
     * @param [key] the selection key to lock.
     * @param [action] the action to execute.
     */
    private fun lockKey(key: SelectionKey, action: () -> Unit) {
        if (key.attachment() == null) {
            key.attach(ReentrantLock())
        }
        val lock = key.attachment() as ReentrantLock
        executor.execute {
            if (lock.tryLock()) {
                action()
                lock.unlock()
            }
        }
    }


    /**
     * Starts the GatewayLBService.
     */
    fun start() {
        logger.info("GatewayLBService стартует")
        while (isRunning) {
            try {
                if (clientSelector.selectNow() > 0) {
                    val keys = clientSelector.selectedKeys()
                    val iterator = keys.iterator()
                    while (iterator.hasNext()) {
                        val key = iterator.next()
                        if (key.isAcceptable) {
                            lockKey(key) {
                                connectToClient()
                                logger.info { "Поток подключил клиент" }
                            }
                        } else if (key.isReadable) {
                            lockKey(key) {
                                logger.info { "Начало обработки запроса клиента" }
                                handleClientRequest(key)
                                logger.info { "Поток обработал запрос клиента" }
                            }
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
                            lockKey(key) {
                                connectToServer()
                                logger.info { "Поток подключил сервер" }
                            }
                        }
                        iterator.remove()
                    }
                }
            } catch (e: CancelledKeyException){
                logger.error { "Кто-то не вовремя отключился" }
            }
        }
    }


    /**
     * Stops the GatewayLBService.
     */
    fun stop() {
        logger.info { "Остановка GatewayLBService..." }
        isRunning = false
        executor.shutdownNow()
        responseExecutor.shutdownNow()
        clientSelector.wakeup()
        serverSelector.wakeup()
        logger.info { "GatewayLBService остановлен" }
    }

    /**
     * Accepts a new client connection and registers it for reading.
     */
    private fun connectToClient() {
        try {
            val clientChannel = clientServerSocketChannel.accept()
            clientChannel.configureBlocking(false)
            clientChannel.register(clientSelector, SelectionKey.OP_READ)
            clientAddress = clientChannel.remoteAddress as InetSocketAddress
            logger.info { "Подключился клиент: $clientAddress" }
        } catch (e: IOException) {
            logger.error("Ошибка при подключении клиента", e)
        }
    }
    /**
     * Connects to a server.
     */
    private fun connectToServer() {
        try {
            val serverChannel = serverServerSocketChannel.accept()
            serverChannel.configureBlocking(false)
            val serverSelector = Selector.open()
            serverChannel.register(serverSelector, SelectionKey.OP_WRITE)
            lock.write {
                servers.add(serverChannel)
            }
            logger.info { "Подключился сервер: ${serverChannel.remoteAddress}. Доступно серверов: ${servers.count()}" }
        } catch (e: IOException) {
            logger.error("Ошибка при подключении сервера", e)
        }
    }
    /**
     * Handles a request received from a client.
     *
     * @param [key] the selection key associated with the client channel.
     */

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
    /**
     * Receive a [Frame] from the given [channel].
     */
    private fun receiveRequest(channel: SocketChannel): Frame {
        val strBuilder = StringBuilder()
        val buffer = ByteBuffer.allocate(1024)
        var tries = 0
        var num = channel.read(buffer)
        while (num == 0) {
            Thread.sleep(500)
            num = channel.read(buffer)
            tries++
            if (tries > 5)
                throw TimeoutException("Нечего читать =(")
        }
        while (num != 0) {
            buffer.flip()
            val len = buffer.limit() - buffer.position()
            val array = ByteArray(len)
            buffer.get(array, buffer.position(), len)
            strBuilder.append(array.decodeToString())
            buffer.flip()
            num = channel.read(buffer)
        }
        val request = serializer.deserialize(strBuilder.toString())
        logger.info { "Получен Frame: ${request.type}" }
        return request
    }
    /**
    * Routes a [request] to an available server and returns its response.
    */
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
    /**
     * Returns the index of the next available server to route requests to.
     * If there are no available servers, throws an exception.
     *
     * @return the index of the next available server to route requests to
     */
    private fun nextIndex(): Int {
        if (servers.isEmpty())
            throw Exception("Пусто")
        else {
            while (true) {
                try {
                    lock.read {
                        if (servers[currentServerIndex].read(ByteBuffer.allocate(1)) == -1)
                            throw SocketException()
                    }
                    break
                } catch (e: SocketException) {
                    lock.write {
                        servers.removeAt(currentServerIndex)
                        logger.info { "Доступно серверов: ${servers.count()}" }
                        if (servers.isEmpty())
                            throw Exception("Пусто")
                        currentServerIndex %= servers.count()
                    }
                }
            }
            lock.write {
                val t = currentServerIndex
                currentServerIndex = (currentServerIndex + 1) % servers.count()
                return t
            }
        }
    }
}