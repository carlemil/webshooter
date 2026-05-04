package se.kjellstrand.webshooter.data.competitionteams.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

interface CompetitionTeamsRemoteDataSource {
    suspend fun getTeams(competitionId: Long): CompetitionTeamsResponse
}

class CompetitionTeamsRemoteDataSourceKtor(
    private val httpClient: HttpClient
) : CompetitionTeamsRemoteDataSource {
    override suspend fun getTeams(competitionId: Long): CompetitionTeamsResponse =
        httpClient.get("api/v4.1.9/competitions/$competitionId/teams").body()
}
