package se.kjellstrand.webshooter.data.competitions.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface CompetitionsRemoteDataSource {

    @GET("/api/v4.1.9/competitions")
    suspend fun getCompetitions(
        @Query("page") page: Int,
        @Query("per_page") perPage: Int,
        @Query("status") status: String,
        @Query("type") type: Int,
    ): CompetitionsResponse
}
