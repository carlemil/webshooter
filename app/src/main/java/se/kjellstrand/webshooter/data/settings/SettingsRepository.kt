package se.kjellstrand.webshooter.data.settings

import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okio.IOException
import retrofit2.HttpException
import se.kjellstrand.webshooter.data.common.Resource
import se.kjellstrand.webshooter.data.common.UserError
import se.kjellstrand.webshooter.data.settings.local.UserProfileDao
import se.kjellstrand.webshooter.data.settings.local.toDomain
import se.kjellstrand.webshooter.data.settings.local.toEntity
import se.kjellstrand.webshooter.data.settings.remote.Club
import se.kjellstrand.webshooter.data.settings.remote.SettingsRemoteDataSource
import se.kjellstrand.webshooter.data.settings.remote.UpdatePasswordRequest
import se.kjellstrand.webshooter.data.settings.remote.UserProfile
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val remoteDataSource: SettingsRemoteDataSource,
    private val dao: UserProfileDao,
    private val gson: Gson
) {

    fun getUserProfile(): Flow<Resource<UserProfile, UserError>> = flow {
        emit(Resource.Loading(true))

        var cached = try {
            dao.get()
        } catch (e: Exception) {
            null
        }
        val profile = cached?.toDomain(gson)
        if (profile != null) {
            emit(Resource.Success(profile))
        } else {
            cached = null
        }

        try {
            val response = remoteDataSource.getUserProfile()
            val profile = response.body()?.user
            if (response.isSuccessful && profile != null) {
                dao.insert(profile.toEntity(gson))
                emit(Resource.Success(profile))
            } else {
                if (cached == null) emit(Resource.Error(UserError.HttpError(response.code())))
            }
        } catch (e: IOException) {
            if (cached == null) emit(Resource.Error(UserError.IOError))
        } catch (e: HttpException) {
            if (cached == null) emit(Resource.Error(UserError.HttpError(e.code())))
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
            val response = remoteDataSource.updateUserProfile(fields)
            val updated = response.body()?.user
            if (response.isSuccessful && updated != null) {
                dao.insert(updated.toEntity(gson))
                emit(Resource.Success(updated))
            } else {
                emit(Resource.Error(UserError.HttpError(response.code())))
            }
        } catch (e: IOException) {
            emit(Resource.Error(UserError.IOError))
        } catch (e: HttpException) {
            emit(Resource.Error(UserError.HttpError(e.code())))
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
            val response = remoteDataSource.updatePassword(fields)
            if (response.isSuccessful) {
                emit(Resource.Success(Unit))
            } else {
                emit(Resource.Error(UserError.HttpError(response.code())))
            }
        } catch (e: IOException) {
            emit(Resource.Error(UserError.IOError))
        } catch (e: HttpException) {
            emit(Resource.Error(UserError.HttpError(e.code())))
        } catch (e: Exception) {
            emit(Resource.Error(UserError.UnknownError))
        }
        emit(Resource.Loading(false))
    }
}
