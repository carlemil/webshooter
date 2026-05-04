package se.kjellstrand.webshooter.data.cookies.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.get

interface CookiesRemoteDataSource {
    suspend fun getCookies()
}

class CookiesRemoteDataSourceKtor(
    private val httpClient: HttpClient
) : CookiesRemoteDataSource {
    override suspend fun getCookies() {
        httpClient.get("/")
    }
}
