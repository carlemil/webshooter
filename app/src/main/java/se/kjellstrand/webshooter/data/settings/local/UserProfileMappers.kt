package se.kjellstrand.webshooter.data.settings.local

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import se.kjellstrand.webshooter.data.settings.remote.Club
import se.kjellstrand.webshooter.data.settings.remote.UserProfile

private const val TAG = "UserProfileMappers"

fun UserProfile.toEntity(gson: Gson): UserProfileEntity = UserProfileEntity(
    userId = userId,
    name = name,
    lastname = lastname,
    email = email,
    shootingCardNumber = shootingCardNumber,
    noShootingCardNumber = noShootingCardNumber,
    birthday = birthday,
    gender = gender,
    phone = phone,
    mobile = mobile,
    gradeField = gradeField,
    gradeTrackshooting = gradeTrackshooting,
    apiToken = apiToken,
    fullname = fullname,
    clubsId = clubsId,
    status = status,
    clubsJson = gson.toJson(clubs)
)

fun UserProfileEntity.toDomain(gson: Gson): UserProfile? = try {
    UserProfile(
        userId = userId,
        name = name,
        lastname = lastname,
        email = email,
        shootingCardNumber = shootingCardNumber,
        noShootingCardNumber = noShootingCardNumber,
        birthday = birthday,
        gender = gender,
        phone = phone,
        mobile = mobile,
        gradeField = gradeField,
        gradeTrackshooting = gradeTrackshooting,
        apiToken = apiToken,
        fullname = fullname,
        clubsId = clubsId,
        status = status,
        clubs = gson.fromJson(clubsJson, object : TypeToken<List<Club>>() {}.type)
    )
} catch (e: JsonSyntaxException) {
    Log.w(TAG, "Error", e)
    null
}
