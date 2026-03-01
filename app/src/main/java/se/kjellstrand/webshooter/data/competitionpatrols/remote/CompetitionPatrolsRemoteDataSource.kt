package se.kjellstrand.webshooter.data.competitionpatrols.remote

import retrofit2.http.GET
import retrofit2.http.Path

interface CompetitionPatrolsRemoteDataSource {
    @GET("api/v4.1.9/competitions/{competitionId}/patrols")
    suspend fun getPatrols(
        @Path("competitionId") competitionId: Long
    ): CompetitionPatrolsResponse
}
