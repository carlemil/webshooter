package se.kjellstrand.webshooter.data.remote

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface LoginRemoteDataSource {

    @POST("api/v4.1.9/oauth/token")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    companion object {
        const val BASE_URL = "https://api.webshooter.api/"
        const val IMAGE_BASE_URL = "https://image.webshooter.api/"
        const val API_KEY = "xxxx"
    }
}
