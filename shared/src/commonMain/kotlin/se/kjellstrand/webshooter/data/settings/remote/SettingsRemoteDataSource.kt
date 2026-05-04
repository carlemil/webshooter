package se.kjellstrand.webshooter.data.settings.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.Parameters

interface SettingsRemoteDataSource {
    suspend fun getUserProfile(): UserProfileResponse
    suspend fun updateUserProfile(fields: Map<String, String>): UserProfileResponse
    suspend fun updatePassword(fields: Map<String, String>)
}

class SettingsRemoteDataSourceKtor(
    private val httpClient: HttpClient
) : SettingsRemoteDataSource {

    override suspend fun getUserProfile(): UserProfileResponse =
        httpClient.get("api/v4.1.9/authenticate/user").body()

    override suspend fun updateUserProfile(fields: Map<String, String>): UserProfileResponse =
        httpClient.put("api/v4.1.9/authenticate/user") {
            setBody(formBody(fields))
        }.body()

    override suspend fun updatePassword(fields: Map<String, String>) {
        httpClient.put("api/v4.1.9/authenticate/updatePassword") {
            setBody(formBody(fields))
        }
    }

    private fun formBody(fields: Map<String, String>): FormDataContent =
        FormDataContent(Parameters.build {
            fields.forEach { (k, v) -> append(k, v) }
        })
}
