import com.google.gson.Gson
import com.google.gson.GsonBuilder
import junit.framework.Assert.assertEquals
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import se.kjellstrand.data.BuildConfig
import se.kjellstrand.webshooter.data.remote.LoginRequest
import se.kjellstrand.webshooter.data.remote.LoginRemoteDataSource

class LoginRemoteDataSourceTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var userApi: LoginRemoteDataSource

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start(8080)

        val gson: Gson = GsonBuilder()
            .setLenient()
            .create()

        userApi = Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)  // Replace with your actual backend URL
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(LoginRemoteDataSource::class.java)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun login_returns_correct_LoginDto_from_real_backend() {
        // Given
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody(
                """{
                "access_token": 1234567890,
                "token_type": "Bearer",
                "expires_in": "31536000"
            }"""
            )
        mockWebServer.enqueue(mockResponse)

        // When
        val request = LoginRequest(
            1,
            "52FphTYzOrmuqHdfgsdfgytzhSEURIJiMFNp6Qt0",
            "user@gmail.com",
            "password",
            "very-secret-password",
            "user@gmail.com"
        )  // Use valid test credentials
        val response = userApi.login(request).execute()

        // Then
        assertEquals(true, response.isSuccessful)
        // You can assert more on the actual content if you know what to expect.
        assert(response.body()?.access_token?.length!! > 9)
        assertEquals("Bearer", response.body()?.token_type)
        assertEquals(31536000, response.body()?.expires_in)
    }
}
