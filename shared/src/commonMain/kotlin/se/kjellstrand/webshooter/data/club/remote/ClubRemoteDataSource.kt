package se.kjellstrand.webshooter.data.club.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

interface ClubRemoteDataSource {
    suspend fun getUserClub(): ClubInfoResponse
}

class ClubRemoteDataSourceKtor(
    private val httpClient: HttpClient
) : ClubRemoteDataSource {
    override suspend fun getUserClub(): ClubInfoResponse =
        httpClient.get("api/v4.1.9/clubs/getUserClub").body()
}
