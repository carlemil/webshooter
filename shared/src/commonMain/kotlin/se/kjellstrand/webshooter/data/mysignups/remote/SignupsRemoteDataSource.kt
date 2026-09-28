package se.kjellstrand.webshooter.data.mysignups.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

interface SignupsRemoteDataSource {
    suspend fun getSignups(): SignupsResponse
}

class SignupsRemoteDataSourceKtor(
    private val httpClient: HttpClient
) : SignupsRemoteDataSource {
    override suspend fun getSignups(): SignupsResponse {
        val response: SignupsResponse = httpClient.get("api/v4.1.9/signup").body()
        return response.copy(groupedSignups = response.groupedSignups.mapValues { (year, group) ->
            if (group.loaded) group
            else httpClient.get("api/v4.1.9/signup/year/$year").body<SignupGroup>()
        })
    }
}
