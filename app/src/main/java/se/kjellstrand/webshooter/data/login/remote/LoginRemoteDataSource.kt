package se.kjellstrand.webshooter.data.login.remote

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface LoginRemoteDataSource {

    @POST("api/v4.1.9/oauth/token")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/v4.1.9/oauth/token")
    fun refreshToken(@Body request: RefreshTokenRequest): Call<LoginResponse>
}
