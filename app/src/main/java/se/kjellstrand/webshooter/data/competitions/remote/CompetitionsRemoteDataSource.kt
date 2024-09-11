package se.kjellstrand.webshooter.data.competitions.remote

import retrofit2.http.Body
import retrofit2.http.POST
import se.kjellstrand.webshooter.data.competitions.remote.parsing.CompetitionsResponse

interface CompetitionsRemoteDataSource {

    @POST("/app/competitions")
    suspend fun getCompetitions(): CompetitionsResponse
}
