package se.kjellstrand.webshooter.data.settings.local

import io.github.aakira.napier.Napier
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import se.kjellstrand.webshooter.data.settings.remote.Club
import se.kjellstrand.webshooter.data.settings.remote.UserProfile

private const val TAG = "UserProfileMappers"

fun UserProfile.toEntity(json: Json): UserProfileEntity = UserProfileEntity(
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
    clubsJson = json.encodeToString(clubs)
)

fun UserProfileEntity.toDomain(json: Json): UserProfile? = try {
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
        clubs = json.decodeFromString<List<Club>>(clubsJson)
    )
} catch (e: SerializationException) {
    Napier.w("Error", e, TAG)
    null
}
