package se.kjellstrand.webshooter.data.club.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ClubRemoteDataSource {

    @GET("api/v4.1.9/clubs/{clubId}")
    suspend fun getClubInfo(
        @Path("clubId") clubId: Long
    ): Response<ClubInfoResponse>

    @GET("api/v4.1.9/clubs/{clubId}/admins")
    suspend fun getClubAdmins(
        @Path("clubId") clubId: Long
    ): Response<ClubAdminsResponse>

    @GET("api/v4.1.9/clubs/{clubId}/users")
    suspend fun getClubUsers(
        @Path("clubId") clubId: Long
    ): Response<ClubUsersResponse>
}
