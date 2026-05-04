package se.kjellstrand.webshooter.data.competitionsignups.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import javax.inject.Inject

interface CompetitionSignupsRemoteDataSource {
    suspend fun getSignups(
        competitionId: Long,
        page: Int,
        perPage: Int
    ): CompetitionSignupsResponse
}

class CompetitionSignupsRemoteDataSourceKtor @Inject constructor(
    private val httpClient: HttpClient
) : CompetitionSignupsRemoteDataSource {
    override suspend fun getSignups(
        competitionId: Long,
        page: Int,
        perPage: Int
    ): CompetitionSignupsResponse =
        httpClient.get("api/v4.1.9/competitions/$competitionId/signups") {
            parameter("page", page)
            parameter("per_page", perPage)
        }.body()
}
