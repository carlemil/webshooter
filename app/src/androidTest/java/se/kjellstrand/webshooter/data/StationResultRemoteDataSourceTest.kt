package se.kjellstrand.webshooter.data

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import se.kjellstrand.webshooter.data.login.remote.LoginRemoteDataSource
import se.kjellstrand.webshooter.data.login.remote.LoginRequest

class StationResultRemoteDataSourceTest {

    private lateinit var mockWebServer: MockWebServer
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
        val contentType = "application/json".toMediaType()

        loginApi = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(LoginRemoteDataSource::class.java)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun login_returns_correct_LoginDto() {
        val mockResponse = MockResponse()
            .setResponseCode(200)
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

        val response = runBlocking {
            loginApi.login(request)
        }

        val body = response.body()
        assertNotNull(body)
        assertTrue((body!!.accessToken.length) > 9)
        assertEquals("Bearer", body.tokenType)
        assertEquals(31536000L, body.expiresIn)
    }
}
