import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.actor
import mu.KotlinLogging
import java.net.SocketException
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import kotlin.coroutines.CoroutineContext


/**
 * Possible commands to the server actor
 */
sealed class ServerCommand {
    class Add(val channel: SocketChannel) : ServerCommand()
    class Remove(val channel: SocketChannel) : ServerCommand()
    class Count(val response: CompletableDeferred<Int>) : ServerCommand()
    class GetNext(val response: CompletableDeferred<SocketChannel?>) : ServerCommand()
}

/**
 * Encapsulates list of connected servers. Makes it possible to use map async and in order
 */
class ServerActor(coroutineContext: CoroutineContext = Dispatchers.IO) {

    private val logger = KotlinLogging.logger {}

    private val scope = CoroutineScope(coroutineContext)
    private val servers = arrayListOf<SocketChannel>()

    private var cur: Int = 0

    @OptIn(ObsoleteCoroutinesApi::class)
    private val commands = scope.actor<ServerCommand>(capacity = Channel.BUFFERED) {
        for (command in this) {
            when (command) {
                is ServerCommand.Add -> servers.add(command.channel)
                is ServerCommand.Remove -> servers.remove(command.channel)
                is ServerCommand.Count -> command.response.complete(servers.count())
                is ServerCommand.GetNext -> {
                    while (servers.isNotEmpty()) {
                        try {
                            if (servers[cur].read(ByteBuffer.allocate(1)) == -1) {
                                throw SocketException()
                            }
                            break
                        } catch (e: SocketException) {
                            servers.removeAt(cur)
                            logger.info { "Доступно серверов: ${servers.count()}" }
                            if (servers.isNotEmpty()) {
                                cur %= servers.count()
                            }
                        }
                    }
                    if (servers.isEmpty()) {
                        command.response.complete(null)
                    } else {
                        command.response.complete(servers[cur])
                        cur = (cur + 1) % servers.count()
                    }
                }

            }
        }
    }


    /**
     * Add connected client
     */
    fun add(channel: SocketChannel) = commands.trySend(ServerCommand.Add(channel))

    /**
     * Remove known client
     */
    fun remove(channel: SocketChannel) = commands.trySend(ServerCommand.Remove(channel))

    /**
     * Count connected servers
     */
    suspend fun count(): Int {
        val count = ServerCommand.Count(CompletableDeferred())
        commands.send(count)
        return count.response.await()
    }

    /**
     * Get next server to process the request
     */
    suspend fun getNext(): SocketChannel? {
        val get = ServerCommand.GetNext(CompletableDeferred())
        commands.send(get)
        return get.response.await()
    }
}