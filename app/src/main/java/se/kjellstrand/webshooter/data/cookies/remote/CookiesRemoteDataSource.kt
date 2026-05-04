package se.kjellstrand.webshooter.data.cookies.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import javax.inject.Inject

interface CookiesRemoteDataSource {
    suspend fun getCookies()
}

class CookiesRemoteDataSourceKtor @Inject constructor(
    private val httpClient: HttpClient
) : CookiesRemoteDataSource {
    override suspend fun getCookies() {
        httpClient.get("/")
    }
}
