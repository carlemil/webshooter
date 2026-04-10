package se.kjellstrand.webshooter.data.competitions.local

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import se.kjellstrand.webshooter.data.common.Club
import se.kjellstrand.webshooter.data.common.CompetitionType
import se.kjellstrand.webshooter.data.common.WeaponClass
import se.kjellstrand.webshooter.data.common.WeaponGroup
import se.kjellstrand.webshooter.data.competitions.remote.Datum
import se.kjellstrand.webshooter.data.competitions.remote.ResultsType
import se.kjellstrand.webshooter.data.competitions.remote.Usersignup

private const val TAG = "CompetitionMappers"

fun Datum.toEntity(gson: Gson): CompetitionEntity = CompetitionEntity(
    id = id,
    name = name,
    date = date,
    status = status,
    statusHuman = statusHuman,
    contactName = contactName,
    contactVenue = contactVenue,
    contactCity = contactCity,
    contactEmail = contactEmail,
    contactTelephone = contactTelephone,
    lat = lat,
    lng = lng,
    googleMaps = googleMaps,
    description = description,
    website = website,
    resultsType = resultsType.name,
    resultsTypeHuman = resultsTypeHuman,
    signupsOpeningDate = signupsOpeningDate,
    signupsClosingDate = signupsClosingDate,
    allowSignupsAfterClosingDateHuman = allowSignupsAfterClosingDateHuman,
    startTimeHuman = startTimeHuman,
    finalTimeHuman = finalTimeHuman,
    signupsCount = signupsCount,
    patrolsCount = patrolsCount,
    allowTeams = allowTeams,
    competitionTypeJson = gson.toJson(competitionType),
    weaponGroupsJson = gson.toJson(weaponGroups),
    weaponClassesJson = gson.toJson(weaponClasses),
    userSignupsJson = gson.toJson(userSignups),
    clubJson = gson.toJson(club)
)

fun CompetitionEntity.toDomain(gson: Gson): Datum? = try {
    Datum(
        id = id,
        name = name,
        allowTeams = allowTeams,
        website = website ?: "",
        contactName = contactName ?: "",
        contactVenue = contactVenue ?: "",
        contactCity = contactCity ?: "",
        lat = lat,
        lng = lng,
        contactEmail = contactEmail ?: "",
        contactTelephone = contactTelephone ?: "",
        googleMaps = googleMaps ?: "",
        description = description ?: "",
        resultsType = ResultsType.valueOf(resultsType),
        date = date,
        signupsOpeningDate = signupsOpeningDate ?: "",
        signupsClosingDate = signupsClosingDate ?: "",
        weaponGroups = gson.fromJson<List<WeaponGroup>?>(weaponGroupsJson, object : TypeToken<List<WeaponGroup>>() {}.type) ?: emptyList(),
        signupsCount = signupsCount,
        patrolsCount = patrolsCount,
        status = status,
        statusHuman = statusHuman,
        startTimeHuman = startTimeHuman ?: "",
        finalTimeHuman = finalTimeHuman ?: "",
        allowSignupsAfterClosingDateHuman = allowSignupsAfterClosingDateHuman ?: "",
        resultsTypeHuman = resultsTypeHuman ?: "",
        competitionType = gson.fromJson(competitionTypeJson, CompetitionType::class.java),
        weaponClasses = gson.fromJson<List<WeaponClass>?>(weaponClassesJson, object : TypeToken<List<WeaponClass>>() {}.type) ?: emptyList(),
        userSignups = gson.fromJson<List<Usersignup>?>(userSignupsJson, object : TypeToken<List<Usersignup>>() {}.type) ?: emptyList(),
        club = gson.fromJson(clubJson, Club::class.java)
    )
} catch (e: JsonSyntaxException) {
    Log.w(TAG, "Error", e)
    null
}
