package se.kjellstrand.webshooter.data.cookies.remote

import retrofit2.Response
import retrofit2.http.GET

interface CookiesRemoteDataSource {

    @GET("/")
    suspend fun getCookies(): Response<Unit>
}