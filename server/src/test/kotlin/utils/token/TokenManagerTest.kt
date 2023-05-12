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
import utils.auth.EncryptManager
import utils.auth.UserStatus
import utils.auth.token.Content
import utils.auth.token.Token
import utils.auth.token.TokenManager
import java.security.MessageDigest
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TokenManagerTest : KoinTest {

    private val myModule = module {
        single {
            val m = mockk<FileManager>()
            every { m.readFile("utils/database/.key") } returns "aboba"
            m
        }
        single { TokenManager(EncryptManager(MessageDigest.getInstance("SHA-384"), get(), "utils/database/.key")) }
    }

    private val content = Content(1, UserStatus.USER)


    @BeforeAll
    fun setup() {
        startKoin {
            modules(myModule)
        }
    }

    @Test
    fun `Untouched token should be ok`() {
        val tokenManager: TokenManager by inject()
        val token = tokenManager.createToken(content)

        assertTrue(tokenManager.checkToken(token))
        assertEquals(content, tokenManager.getContent(token))
    }

    @Test
    fun `Expired token shouldn't be ok`() {
        val tokenManager: TokenManager by inject()
        val token = tokenManager.createToken(content, -1)

        assertFalse(tokenManager.checkToken(token))
    }


    @Test
    fun `Modified token shouldn't be ok`() {
        val tokenManager: TokenManager by inject()
        val token = tokenManager.createToken(content)

        val parts = token.toString().split('.').toMutableList()
        parts[1] = Base64.getEncoder().encodeToString(Json.encodeToString(Content(1, UserStatus.ADMIN)).toByteArray())
        val falseToken = Token(parts[0], parts[1], parts[2])

        assertFalse(tokenManager.checkToken(falseToken))
    }

    @Test
    fun `Token parse works`() {
        val tokenManager: TokenManager by inject()
        val token = tokenManager.createToken(content)
        val str = token.toString()

        assertEquals(token, Token.parse(str))
        assertThrows<IllegalArgumentException> {
            Token.parse("aboba")
        }
    }
}