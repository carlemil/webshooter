package se.kjellstrand.webshooter.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface UserRemoteDataSource {

    @GET("user/{pistolShooterCardNumber}")
    suspend fun getUser(
        @Path("pistolShooterCardNumber") pistolShooterCardNumber: String,
        @Query("page") page: Int,
        @Query("api_key") apiKey: String = API_KEY
    ): UserDto

    @GET("users")
    suspend fun getAllUsers(
        @Query("page") page: Int,
        @Query("api_key") apiKey: String = API_KEY
    ): List<UserDto>

    companion object {
        const val BASE_URL = "https://api.webshooter.api/"
        const val IMAGE_BASE_URL = "https://image.webshooter.api/"
        const val API_KEY = "xxxx"
    }
}
