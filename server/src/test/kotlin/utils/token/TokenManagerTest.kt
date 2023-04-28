package utils.token

import FileManager
import io.mockk.every
import io.mockk.mockk
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.koin.core.context.GlobalContext.startKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject
import java.security.MessageDigest
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TokenManagerTest : KoinTest {

    private val myModule = module {
        single {
            val m = mockk<FileManager>()
            every { m.readFile(".key") } returns "aboba"
            m
        }
        single { TokenManager(MessageDigest.getInstance("SHA-384"), get(), ".key") }
    }

    @BeforeAll
    fun setup() {
        startKoin {
            modules(myModule)
        }
    }

    @Test
    fun `Untouched token should be ok`() {
        val tokenManager: TokenManager by inject()
        val content = Content(1, "user")
        val token = tokenManager.createToken(content)

        assertTrue(tokenManager.checkToken(token))
        assertEquals(content, tokenManager.getContent(token))
    }

    @Test
    fun `Expired token shouldn't be ok`() {
        val tokenManager: TokenManager by inject()
        val content = Content(1, "user")
        val token = tokenManager.createToken(content, -1)

        assertFalse(tokenManager.checkToken(token))
    }


    @Test
    fun `Modified token shouldn't be ok`() {
        val tokenManager: TokenManager by inject()
        val content = Content(1, "user")
        val token = tokenManager.createToken(content)

        val parts = token.toString().split('.').toMutableList()
        parts[1] = Base64.getEncoder().encodeToString(Json.encodeToString(Content(1, "admin")).toByteArray())
        val falseToken = Token(parts[0], parts[1], parts[2])

        println(token)
        println(falseToken)


        assertFalse(tokenManager.checkToken(falseToken))
    }

    @Test
    fun `Token parse works`() {
        val tokenManager: TokenManager by inject()
        val content = Content(1, "user")
        val token = tokenManager.createToken(content)
        val str = token.toString()

        assertEquals(token, Token.parse(str))
        assertThrows<IllegalArgumentException> {
            Token.parse("aboba")
        }
    }
}