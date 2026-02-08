package se.kjellstrand.webshooter.data.results.remote

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface ResultsRemoteDataSource {

    @GET("/api/v4.1.9/competitions/{id}/results")
    suspend fun getResults(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
    ): ResultsResponse
}
