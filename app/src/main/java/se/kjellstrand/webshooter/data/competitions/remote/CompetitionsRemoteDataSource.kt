package se.kjellstrand.webshooter.data.competitions.remote

import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface CompetitionsRemoteDataSource {

    @POST("/app/competitions")
    suspend fun getCompetitions(
        @Header("Authorization") token: String,
        @Query("page") page: Int,
        @Query("per_page") perPage: Int,
        @Query("status") status: String,
        @Query("type") type: Int,
    ): CompetitionsResponse
}
