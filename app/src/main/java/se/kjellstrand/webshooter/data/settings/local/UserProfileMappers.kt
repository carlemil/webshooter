package se.kjellstrand.webshooter.data.settings.local

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import se.kjellstrand.webshooter.data.settings.remote.Club
import se.kjellstrand.webshooter.data.settings.remote.UserProfile

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

fun UserProfileEntity.toDomain(gson: Gson): UserProfile = UserProfile(
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
