package se.kjellstrand.webshooter.data.settings

import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.ResponseException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import okio.IOException
import se.kjellstrand.webshooter.data.common.Resource
import se.kjellstrand.webshooter.data.common.UserError
import se.kjellstrand.webshooter.data.settings.local.UserProfileDao
import se.kjellstrand.webshooter.data.settings.local.toDomain
import se.kjellstrand.webshooter.data.settings.local.toEntity
import se.kjellstrand.webshooter.data.settings.remote.SettingsRemoteDataSource
import se.kjellstrand.webshooter.data.settings.remote.UserProfile




class SettingsRepository constructor(
    private val remoteDataSource: SettingsRemoteDataSource,
    private val dao: UserProfileDao,
    private val json: Json
) {

    fun getUserProfile(): Flow<Resource<UserProfile, UserError>> = flow {
        emit(Resource.Loading(true))

        var cached = try {
            dao.get()
        } catch (e: Exception) {
            null
        }
        val profile = cached?.toDomain(json)
        if (profile != null) {
            emit(Resource.Success(profile))
        } else {
            cached = null
        }

        try {
            val updated = remoteDataSource.getUserProfile().user
            dao.insert(updated.toEntity(json))
            emit(Resource.Success(updated))
        } catch (e: ResponseException) {
            if (cached == null) emit(Resource.Error(UserError.HttpError(e.response.status.value, e.response.call.request.url.encodedPath)))
        } catch (e: SocketTimeoutException) {
            if (cached == null) emit(Resource.Error(UserError.IOError))
        } catch (e: ConnectTimeoutException) {
            if (cached == null) emit(Resource.Error(UserError.IOError))
        } catch (e: IOException) {
            if (cached == null) emit(Resource.Error(UserError.IOError))
        } catch (e: Exception) {
            if (cached == null) emit(Resource.Error(UserError.UnknownError))
        }
        emit(Resource.Loading(false))
    }

    fun updateUserProfile(profile: UserProfile): Flow<Resource<UserProfile, UserError>> = flow {
        emit(Resource.Loading(true))
        try {
            val fields = buildMap<String, String> {
                put("name", profile.name)
                put("lastname", profile.lastname)
                put("email", profile.email)
                put("shooting_card_number", profile.shootingCardNumber ?: "")
                put("no_shooting_card_number", profile.noShootingCardNumber ?: "")
                put("birthday", profile.birthday ?: "")
                put("gender", profile.gender ?: "")
                put("phone", profile.phone ?: "")
                put("mobile", profile.mobile ?: "")
                put("grade_field", profile.gradeField ?: "")
                put("grade_trackshooting", profile.gradeTrackshooting ?: "")
                put("api_token", profile.apiToken ?: "")
                put("user_id", profile.userId.toString())
                put("fullname", profile.fullname)
                put("clubs_id", profile.clubsId.toString())
                put("status", profile.status)
                profile.clubs.forEachIndexed { i, club ->
                    put("clubs[$i][id]", club.id.toString())
                    put("clubs[$i][clubs_nr]", club.clubsNr?.toString() ?: "null")
                    put("clubs[$i][name]", club.name)
                    put("clubs[$i][email]", club.email ?: "null")
                    put("clubs[$i][phone]", club.phone ?: "null")
                    put("clubs[$i][address_street]", club.addressStreet ?: "null")
                    put("clubs[$i][address_zipcode]", club.addressZipcode ?: "null")
                    put("clubs[$i][address_city]", club.addressCity ?: "null")
                    put("clubs[$i][address_country]", club.addressCountry ?: "null")
                    put("clubs[$i][bankgiro]", club.bankgiro ?: "null")
                    put("clubs[$i][postgiro]", club.postgiro ?: "null")
                    put("clubs[$i][swish]", club.swish ?: "")
                }
            }
            val updated = remoteDataSource.updateUserProfile(fields).user
            dao.insert(updated.toEntity(json))
            emit(Resource.Success(updated))
        } catch (e: ResponseException) {
            emit(Resource.Error(UserError.HttpError(e.response.status.value, e.response.call.request.url.encodedPath)))
        } catch (e: SocketTimeoutException) {
            emit(Resource.Error(UserError.IOError))
        } catch (e: ConnectTimeoutException) {
            emit(Resource.Error(UserError.IOError))
        } catch (e: IOException) {
            emit(Resource.Error(UserError.IOError))
        } catch (e: Exception) {
            emit(Resource.Error(UserError.UnknownError))
        }
        emit(Resource.Loading(false))
    }

    fun updatePassword(
        currentPassword: String,
        newPassword: String,
        confirmation: String
    ): Flow<Resource<Unit, UserError>> = flow {
        emit(Resource.Loading(true))
        try {
            val fields = mapOf(
                "current_password" to currentPassword,
                "password" to newPassword,
                "password_confirmation" to confirmation
            )
            remoteDataSource.updatePassword(fields)
            emit(Resource.Success(Unit))
        } catch (e: ResponseException) {
            emit(Resource.Error(UserError.HttpError(e.response.status.value, e.response.call.request.url.encodedPath)))
        } catch (e: SocketTimeoutException) {
            emit(Resource.Error(UserError.IOError))
        } catch (e: ConnectTimeoutException) {
            emit(Resource.Error(UserError.IOError))
        } catch (e: IOException) {
            emit(Resource.Error(UserError.IOError))
        } catch (e: Exception) {
            emit(Resource.Error(UserError.UnknownError))
        }
        emit(Resource.Loading(false))
    }
}
