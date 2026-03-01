package se.kjellstrand.webshooter.data.competitionsignups.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CompetitionSignupsRemoteDataSource {
    @GET("api/v4.1.9/competitions/{competitionId}/signups")
    suspend fun getSignups(
        @Path("competitionId") competitionId: Long,
        @Query("page") page: Int,
        @Query("per_page") perPage: Int
    ): CompetitionSignupsResponse
}
