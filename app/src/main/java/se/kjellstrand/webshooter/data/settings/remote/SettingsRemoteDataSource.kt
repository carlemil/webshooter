package se.kjellstrand.webshooter.data.settings.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.PUT

interface SettingsRemoteDataSource {

    @GET("api/v4.1.9/authenticate/user")
    suspend fun getUserProfile(): Response<UserProfileResponse>

    @FormUrlEncoded
    @PUT("api/v4.1.9/authenticate/user")
    suspend fun updateUserProfile(@FieldMap fields: Map<String, String>): Response<UserProfileResponse>

    @FormUrlEncoded
    @PUT("api/v4.1.9/authenticate/updatePassword")
    suspend fun updatePassword(@FieldMap fields: Map<String, String>): Response<Unit>
}
