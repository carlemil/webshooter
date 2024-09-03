package se.kjellstrand.webshooter.data

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import se.kjellstrand.data.BuildConfig
import se.kjellstrand.webshooter.data.remote.UserRemoteDataSource

class UserRemoteDataSourceTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var userApi: UserRemoteDataSource

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start(8080)

        val gson: Gson = GsonBuilder()
            .setLenient()
            .create()

        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(OkHttpClient.Builder().build())
            .build()

        userApi = retrofit.create(UserRemoteDataSource::class.java)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun getUser_returns_correct_UserDto() {
        // Given
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody(
                """{
                "pistolShooterCardNumber": 12345,
                "firstName": "John",
                "lastName": "Doe"
            }"""
            )
        mockWebServer.enqueue(mockResponse)

        // When
        val category = "someCategory"
        val page = 1
        val response = runBlocking {
            userApi.getUser(category, page)
        }

        // Then
        assertEquals(12345, response.pistolShooterCardNumber)
        assertEquals("John", response.firstName)
        assertEquals("Doe", response.lastName)

        val recordedRequest = mockWebServer.takeRequest()
        assertEquals("/user/someCategory?page=1", recordedRequest.path)
    }
}
