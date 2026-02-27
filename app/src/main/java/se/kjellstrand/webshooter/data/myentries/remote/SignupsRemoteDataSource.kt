package se.kjellstrand.webshooter.data.myentries.remote

import retrofit2.http.GET

interface SignupsRemoteDataSource {

    @GET("api/v4.1.9/signup")
    suspend fun getSignups(): SignupsResponse
}
