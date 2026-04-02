package se.kjellstrand.webshooter.data.competitionteams.remote

import retrofit2.http.GET
import retrofit2.http.Path

interface CompetitionTeamsRemoteDataSource {
    @GET("api/v4.1.9/competitions/{competitionId}/teams")
    suspend fun getTeams(
        @Path("competitionId") competitionId: Long
    ): CompetitionTeamsResponse
}
