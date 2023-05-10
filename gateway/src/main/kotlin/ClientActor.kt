import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.actor
import java.nio.channels.SocketChannel
import kotlin.coroutines.CoroutineContext

sealed class ClientCommand {
    class Add(val channel: SocketChannel) : ClientCommand()
    class Remove(val channel: SocketChannel) : ClientCommand()
    class Get(val address: String, val response: CompletableDeferred<SocketChannel?>) : ClientCommand()
}


class ClientActor(coroutineContext: CoroutineContext = Dispatchers.Unconfined) {

    private val scope = CoroutineScope(coroutineContext)
    private val clients = HashMap<String, SocketChannel>()

    @OptIn(ObsoleteCoroutinesApi::class)
    private val commands = scope.actor<ClientCommand>(capacity = Channel.BUFFERED) {
        for (command in this) {
            when (command) {
                is ClientCommand.Add -> clients[command.channel.remoteAddress.toString()] = command.channel
                is ClientCommand.Remove -> clients.remove(command.channel.remoteAddress.toString())
                is ClientCommand.Get -> command.response.complete(clients[command.address])
            }
        }
    }

    fun add(channel: SocketChannel) = commands.trySend(ClientCommand.Add(channel))
    fun remove(channel: SocketChannel) = commands.trySend(ClientCommand.Remove(channel))

    suspend fun get(address: String): SocketChannel? {
        val get = ClientCommand.Get(address, CompletableDeferred())
        commands.send(get)
        return get.response.await()
    }
}