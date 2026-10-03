package se.kjellstrand.webshooter.data.results.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import se.kjellstrand.webshooter.data.BackgroundRequest

interface ResultsRemoteDataSource {
    suspend fun getResults(id: Long): ResultsResponse

    /** Same call, flagged as bulk-sync work so it yields to interactive requests. */
    suspend fun getResultsInBackground(id: Long): ResultsResponse = getResults(id)
}

class ResultsRemoteDataSourceKtor(
    private val httpClient: HttpClient
) : ResultsRemoteDataSource {
    override suspend fun getResults(id: Long): ResultsResponse =
        httpClient.get("api/v4.1.9/competitions/$id/results").body()

    override suspend fun getResultsInBackground(id: Long): ResultsResponse =
        httpClient.get("api/v4.1.9/competitions/$id/results") {
            attributes.put(BackgroundRequest, Unit)
        }.body()
}
