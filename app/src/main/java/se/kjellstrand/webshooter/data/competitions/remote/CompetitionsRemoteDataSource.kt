package se.kjellstrand.webshooter.data.competitions.remote

import retrofit2.http.POST

interface CompetitionsRemoteDataSource {

    @POST("/app/competitions")
    suspend fun getCompetitions(): CompetitionsResponse
}
