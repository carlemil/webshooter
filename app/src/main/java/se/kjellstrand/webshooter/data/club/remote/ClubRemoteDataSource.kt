package se.kjellstrand.webshooter.data.club.remote

import retrofit2.Response
import retrofit2.http.GET

interface ClubRemoteDataSource {

    @GET("api/v4.1.9/clubs/getUserClub")
    suspend fun getUserClub(): Response<ClubInfoResponse>
}
