package se.kjellstrand.webshooter.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface UserApi {

    @GET("user/{pistolShooterCardNumber}")
    suspend fun getUser(
        @Path("category") category: String,
        @Query("page") page: Int,
        @Query("api_key") apiKey: String = API_KEY
    ): UserDto

    companion object {
        const val BASE_URL = "https://api.webshooter.api/"
        const val IMAGE_BASE_URL = "https://image.webshooter.api/"
        const val API_KEY = "xxxx"
    }
}
