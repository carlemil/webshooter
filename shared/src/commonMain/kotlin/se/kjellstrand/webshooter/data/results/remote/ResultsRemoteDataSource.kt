package se.kjellstrand.webshooter.data.results.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

interface ResultsRemoteDataSource {
    suspend fun getResults(id: Long): ResultsResponse
}

class ResultsRemoteDataSourceKtor(
    private val httpClient: HttpClient
) : ResultsRemoteDataSource {
    override suspend fun getResults(id: Long): ResultsResponse =
        httpClient.get("api/v4.1.9/competitions/$id/results").body()
}
