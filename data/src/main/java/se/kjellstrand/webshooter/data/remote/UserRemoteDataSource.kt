package se.kjellstrand.webshooter.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface UserRemoteDataSource {

    @GET("user/{pistolShooterCardNumber}")
    suspend fun getUser(
        @Path("pistolShooterCardNumber") pistolShooterCardNumber: String,
        @Query("page") page: Int
    ): UserRequest

    @GET("users")
    suspend fun getAllUsers(
        @Query("page") page: Int
    ): List<UserRequest>
}
