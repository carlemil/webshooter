package se.kjellstrand.webshooter.data.competitions.local

import io.github.aakira.napier.Napier
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.kotlincrypto.hash.sha1.SHA1
import se.kjellstrand.webshooter.data.common.Club
import se.kjellstrand.webshooter.data.common.CompetitionType
import se.kjellstrand.webshooter.data.common.WeaponClass
import se.kjellstrand.webshooter.data.common.WeaponGroup
import se.kjellstrand.webshooter.data.competitions.remote.Datum
import se.kjellstrand.webshooter.data.competitions.remote.ResultsType
import se.kjellstrand.webshooter.data.competitions.remote.Usersignup

private const val TAG = "CompetitionMappers"

fun CompetitionEntity.contentHash(): String {
    val joined = listOf(
        id, name, date, status, statusHuman,
        contactName, contactVenue, contactCity, contactEmail, contactTelephone,
        lat, lng, googleMaps, description, website,
        resultsType, resultsTypeHuman,
        signupsOpeningDate, signupsClosingDate, allowSignupsAfterClosingDateHuman,
        startTimeHuman, finalTimeHuman,
        signupsCount, patrolsCount, allowTeams,
        competitionTypeJson, weaponGroupsJson, weaponClassesJson, userSignupsJson, clubJson
    ).joinToString("") { it?.toString() ?: " " }
    val bytes = SHA1().digest(joined.encodeToByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
}

fun Datum.toEntity(json: Json): CompetitionEntity = CompetitionEntity(
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
    lat = lat ?: 0.0,
    lng = lng ?: 0.0,
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
    competitionTypeJson = json.encodeToString(competitionType),
    weaponGroupsJson = json.encodeToString(weaponGroups),
    weaponClassesJson = json.encodeToString(weaponClasses),
    userSignupsJson = json.encodeToString(userSignups),
    clubJson = json.encodeToString(club)
)

fun CompetitionEntity.toDomain(json: Json): Datum? = try {
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
        weaponGroups = json.decodeFromString<List<WeaponGroup>>(weaponGroupsJson),
        signupsCount = signupsCount,
        patrolsCount = patrolsCount,
        status = status,
        statusHuman = statusHuman,
        startTimeHuman = startTimeHuman ?: "",
        finalTimeHuman = finalTimeHuman ?: "",
        allowSignupsAfterClosingDateHuman = allowSignupsAfterClosingDateHuman ?: "",
        resultsTypeHuman = resultsTypeHuman ?: "",
        competitionType = json.decodeFromString<CompetitionType>(competitionTypeJson),
        weaponClasses = json.decodeFromString<List<WeaponClass>>(weaponClassesJson),
        userSignups = json.decodeFromString<List<Usersignup>>(userSignupsJson),
        club = json.decodeFromString<Club>(clubJson)
    )
} catch (e: SerializationException) {
    Napier.w("Error", e, TAG)
    null
}
