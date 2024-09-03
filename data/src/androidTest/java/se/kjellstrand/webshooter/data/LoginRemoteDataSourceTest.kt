import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import se.kjellstrand.webshooter.data.remote.LoginRequest
import se.kjellstrand.webshooter.data.remote.LoginRemoteDataSource

class LoginRemoteDataSourceTest {

    private lateinit var userApi: LoginRemoteDataSource

    @Before
    fun setUp() {
        userApi = Retrofit.Builder()
            .baseUrl("https://webshooter.se/")  // Replace with your actual backend URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LoginRemoteDataSource::class.java)
    }

    @Test
    fun login_returns_correct_LoginDto_from_real_backend() {
        // When
        val request = LoginRequest(
            1,
            "52FphTYzOrmuqH30ltL7LrBzhSEURIJiMFNp6Qt0",
            "erbsman@gmail.com",
            "password",
            "6nRgbXK3urbFsyi",
            "erbsman@gmail.com"
        )  // Use valid test credentials
        val response = userApi.login(request).execute()

        // Then
        assertEquals(true, response.isSuccessful)
        // You can assert more on the actual content if you know what to expect.
        assert(response.body()?.access_token?.length!! > 10)
        assertEquals("Bearer", response.body()?.token_type)
        assertEquals(31536000, response.body()?.expires_in)
    }
}
