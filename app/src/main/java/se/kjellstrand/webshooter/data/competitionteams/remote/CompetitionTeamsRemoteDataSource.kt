package se.kjellstrand.webshooter.data.competitionteams.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import javax.inject.Inject

interface CompetitionTeamsRemoteDataSource {
    suspend fun getTeams(competitionId: Long): CompetitionTeamsResponse
}

class CompetitionTeamsRemoteDataSourceKtor @Inject constructor(
    private val httpClient: HttpClient
) : CompetitionTeamsRemoteDataSource {
    override suspend fun getTeams(competitionId: Long): CompetitionTeamsResponse =
        httpClient.get("api/v4.1.9/competitions/$competitionId/teams").body()
}
