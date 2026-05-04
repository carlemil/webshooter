package se.kjellstrand.webshooter.data.competitions.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

interface CompetitionsRemoteDataSource {
    suspend fun getCompetitions(
        page: Int,
        perPage: Int,
        status: String,
        type: Int,
        userSignup: Int
    ): CompetitionsResponse
}

class CompetitionsRemoteDataSourceKtor(
    private val httpClient: HttpClient
) : CompetitionsRemoteDataSource {
    override suspend fun getCompetitions(
        page: Int,
        perPage: Int,
        status: String,
        type: Int,
        userSignup: Int
    ): CompetitionsResponse =
        httpClient.get("api/v4.1.9/competitions") {
            parameter("page", page)
            parameter("per_page", perPage)
            parameter("status", status)
            parameter("type", type)
            parameter("usersignup", userSignup)
        }.body()
}
