package se.kjellstrand.webshooter.data.signup.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Parameters
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject

interface SignupRemoteDataSource {
    suspend fun signup(fields: Map<String, String>): SignupResponse
    suspend fun removeSignup(signupId: Long)
}

class SignupRemoteDataSourceKtor @Inject constructor(
    private val httpClient: HttpClient
) : SignupRemoteDataSource {

    override suspend fun signup(fields: Map<String, String>): SignupResponse =
        httpClient.post("api/v4.1.9/signup") {
            setBody(FormDataContent(Parameters.build {
                fields.forEach { (k, v) -> append(k, v) }
            }))
        }.body()

    override suspend fun removeSignup(signupId: Long) {
        httpClient.delete("api/v4.1.9/signup/$signupId")
    }
}

@Serializable
data class SignupResponse(
    val signup: SignupData?
)

@Serializable
data class SignupData(
    val id: Long,
    @SerialName("competitions_id") val competitionsId: Long,
    @SerialName("weapon_classes_id") val weaponClassesId: Long
)
