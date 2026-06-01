package se.kjellstrand.webshooter.data

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.DefaultRequest
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import se.kjellstrand.webshooter.data.login.remote.LoginRemoteDataSource
import se.kjellstrand.webshooter.data.login.remote.LoginRemoteDataSourceKtor
import se.kjellstrand.webshooter.data.login.remote.LoginRequest

class StationResultRemoteDataSourceTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var httpClient: HttpClient
    private lateinit var loginApi: LoginRemoteDataSource

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            explicitNulls = false
        }

        httpClient = HttpClient(OkHttp) {
            expectSuccess = true
            install(ContentNegotiation) { json(json) }
            install(DefaultRequest) {
                url(mockWebServer.url("/").toString())
            }
        }

        loginApi = LoginRemoteDataSourceKtor(httpClient)
    }

    @After
    fun tearDown() {
        httpClient.close()
        mockWebServer.shutdown()
    }

    @Test
    fun login_returns_correct_LoginDto() {
        // The real webshooter.se OAuth endpoint returns Content-Type:
        // application/json; that header is what triggers Ktor's
        // ContentNegotiation plugin to deserialize. Without it, .body()
        // throws NoTransformationFoundException.
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody(
                """{
                "access_token": "1234567890",
                "token_type": "Bearer",
                "expires_in": 31536000,
                "refresh_token": "rtoken-abc"
            }"""
            )
        mockWebServer.enqueue(mockResponse)

        val request = LoginRequest(
            client_id = 1,
            client_secret = "test-client-secret",
            email = "test@example.com",
            password = "test-password",
            username = "test@example.com"
        )

        val body = runBlocking {
            loginApi.login(request)
        }

        assertTrue((body.accessToken.length) > 9)
        assertEquals("Bearer", body.tokenType)
        assertEquals(31536000L, body.expiresIn)
    }
}
