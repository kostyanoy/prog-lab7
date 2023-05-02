//package utils
//import ServerApp
//import io.mockk.every
//import io.mockk.mockk
//import io.mockk.verify
//import org.junit.jupiter.api.Test
//import org.koin.test.KoinTest
//import java.nio.channels.SelectionKey
//import java.nio.channels.ServerSocketChannel
//import java.nio.channels.SocketChannel
//
//class ServerTest:KoinTest {
//    private val serverApp = ServerApp(2228)
//    private val key = mockk<SelectionKey>(relaxed = true)
//    private val serverSocketChannel = mockk<ServerSocketChannel>(relaxed = true)
//    private val socketChannel = mockk<SocketChannel>(relaxed = true)
//    @Test
//    fun `Accept connection`() {
//        every { key.channel() } returns serverSocketChannel
//        every { serverSocketChannel.accept() } returns socketChannel
//        every { socketChannel.configureBlocking(any()) } returns socketChannel
//
//        serverApp.acceptConnection(key, mockk(relaxed = true))
//
//        verify { serverSocketChannel.accept() }
//        verify { socketChannel.configureBlocking(false) }
//        verify { socketChannel.register(any(), SelectionKey.OP_READ) }
//    }
//
//}