package se.kjellstrand.webshooter.data.competitionpatrols.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import javax.inject.Inject

interface CompetitionPatrolsRemoteDataSource {
    suspend fun getPatrols(competitionId: Long): CompetitionPatrolsResponse
}

class CompetitionPatrolsRemoteDataSourceKtor @Inject constructor(
    private val httpClient: HttpClient
) : CompetitionPatrolsRemoteDataSource {
    override suspend fun getPatrols(competitionId: Long): CompetitionPatrolsResponse =
        httpClient.get("api/v4.1.9/competitions/$competitionId/patrols").body()
}
