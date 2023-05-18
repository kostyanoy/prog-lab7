import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import mu.KotlinLogging
import org.koin.core.component.KoinComponent
import serialize.FrameSerializer
import java.io.IOException
import java.net.InetSocketAddress
import java.net.SocketException
import java.nio.ByteBuffer
import java.nio.channels.*

/**
 * Gateway Load Balancer service.
 *
 * @param [clientPort] the port number for incoming client connections.
 * @param [serverPort] the port number for outgoing server connections.
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
    private val servers = ServerActor()
    private val clients = ClientActor()

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
    private suspend fun lockKey(key: SelectionKey, action: suspend () -> Job) = withContext(Dispatchers.IO) {
        if (key.attachment() == null) {
            key.attach(Mutex())
        }
        val mutex = key.attachment() as Mutex
        if (mutex.tryLock()) {
            action().join()
            mutex.unlock()
        }
    }

    /**
     * Starts the GatewayLBService.
     */
    suspend fun start() = withContext(Dispatchers.IO) {
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
                                logger.info { "Вижу клиент хочет подключиться..." }
                                launch {
                                    connectToClient()
                                    logger.info { "Поток подключил клиент" }
                                }
                            }
                        } else if (key.isReadable) {
                            lockKey(key) {
                                logger.info { "Вижу клиент хочет писать..." }
                                launch {
                                    logger.info { "Начало обработки запроса клиента" }
                                    handleClientRequest(key)
                                    logger.info { "Поток обработал запрос клиента" }
                                }
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
                            logger.info { "Вижу сервер хочет подключиться..." }
                            lockKey(key) {
                                launch {
                                    connectToServer()
                                    logger.info { "Поток подключил сервер" }
                                }
                            }
                        } else if (key.isReadable) {
                            logger.info { "Вижу сервер хочет писать..." }
                            lockKey(key) {
                                launch {
                                    logger.info { "Начало обработки запроса сервера" }
                                    handleServerRequest(key)
                                    logger.info { "Поток обработал запрос сервера" }
                                }
                            }
                        }
                    }
                    iterator.remove()
                }
            } catch (e: CancelledKeyException) {
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
        clientSelector.wakeup()
        serverSelector.wakeup()
        clientSelector.close()
        serverSelector.close()
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
            clients.add(clientChannel)
            logger.info { "Подключился клиент: ${clientChannel.remoteAddress}" }
        } catch (e: IOException) {
            logger.error("Ошибка при подключении клиента", e)
        }
    }

    /**
     * Removes server from the connected list
     *
     * @param serverChannel server's channel
     */
    private fun removeServer(serverChannel: SocketChannel) {
        logger.info { "Отключен сервер ${serverChannel.remoteAddress}" }
        servers.remove(serverChannel)
        serverChannel.close()
    }

    /**
     * Connects to a server.
     */
    private suspend fun connectToServer() = withContext(Dispatchers.IO) {
        try {
            val serverChannel = serverServerSocketChannel.accept()
            servers.add(serverChannel)
            val countDef = async { servers.count() }
            serverChannel.configureBlocking(false)
            serverChannel.register(serverSelector, SelectionKey.OP_READ)
            logger.info("Подключился сервер: ${serverChannel.remoteAddress}. Доступно серверов: ${countDef.await()}")
        } catch (e: IOException) {
            logger.error("Ошибка при подключении сервера", e)
        }
    }

    /**
     * Removes client from the connected list
     *
     * @param clientChannel client's channel
     */
    private fun removeClient(clientChannel: SocketChannel) {
        logger.info { "Отключен клиент ${clientChannel.remoteAddress}" }
        clients.remove(clientChannel)
        clientChannel.close()
    }

    /**
     * Handles a request received from a client.
     *
     * @param [key] the selection key associated with the client channel.
     */

    private suspend fun handleClientRequest(key: SelectionKey) = withContext(Dispatchers.IO) {
        val clientChannel = key.channel() as SocketChannel
        try {
            val serverDef = async { servers.getNext() }
            val request = receiveRequest(clientChannel)
            if (request.type == FrameType.EXIT) {
                removeClient(clientChannel)
                serverDef.cancel()
                return@withContext
            }
            request.setValue("address", clientChannel.remoteAddress.toString())
            launch { sendFrame(serverDef, request) }
            val server = serverDef.await() ?: throw Exception("Нет доступных серверов")
            logger.info { "Маршрутизирован Frame к ${server.remoteAddress}" }
        } catch (e: SocketException) {
            removeClient(clientChannel)
        } catch (e: Exception) {
            logger.error("Ошибка обработки запроса от клиента", e)
            removeClient(clientChannel)
        }
    }

    /**
     * Handles a request received from a server.
     *
     * @param [key] the selection key associated with the server channel.
     */
    private suspend fun handleServerRequest(key: SelectionKey) = withContext(Dispatchers.IO) {
        val serverChannel = key.channel() as SocketChannel
        try {
            val request = receiveRequest(serverChannel)
            if (request.type == FrameType.EXIT) {
                removeServer(serverChannel)
                return@withContext
            }
            val address = request.body["address"] as? String ?: throw Exception("Сервер не передал адрес клиента")
            val clientDef = async { clients.get(address) }
            launch { sendFrame(clientDef, request) }
            val client = clientDef.await() ?: throw Exception("Клиент $address не найден")
            logger.info { "Отправлен ответ на клиент ${client.remoteAddress}" }
        } catch (e: SocketException) {
            removeServer(serverChannel)
        } catch (e: Exception) {
            logger.error("Ошибка обработки ответа от сервера", e)
            removeServer(serverChannel)
        }
    }

    /**
     * Receive a [Frame] from the given [channel].
     */
    private fun receiveRequest(channel: SocketChannel): Frame {
        val strBuilder = StringBuilder()
        val buffer = ByteBuffer.allocate(1024)
        var num = channel.read(buffer)
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
     * Send a [Frame] to the given channel. Awaits for it
     *
     * @param channelDef the deferred value of channel so frame could be serialized async
     */
    private suspend fun sendFrame(channelDef: Deferred<SocketChannel?>, response: Frame) = withContext(Dispatchers.IO) {
        val buffer = ByteBuffer.wrap((serializer.serialize(response) + '\n').toByteArray())
        val channel = channelDef.await() ?: throw Exception("Канал не найден")
        channel.write(buffer)
        logger.info { "Отправлен frame на ${channel.remoteAddress}" }
    }
}