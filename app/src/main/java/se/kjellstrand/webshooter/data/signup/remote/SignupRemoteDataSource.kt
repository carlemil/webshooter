package se.kjellstrand.webshooter.data.signup.remote

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface SignupRemoteDataSource {

    @FormUrlEncoded
    @POST("api/v4.1.9/signup")
    suspend fun signup(
        @FieldMap fields: Map<String, String>
    ): Response<SignupResponse>
}

data class SignupResponse(
    val signup: SignupData?
)

data class SignupData(
    val id: Long,
    @SerializedName("competitions_id") val competitionsId: Long,
    @SerializedName("weapon_classes_id") val weaponClassesId: Long
)
