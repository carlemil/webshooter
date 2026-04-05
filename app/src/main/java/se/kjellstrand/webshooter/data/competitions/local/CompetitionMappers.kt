package se.kjellstrand.webshooter.data.competitions.local

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import se.kjellstrand.webshooter.data.common.Club
import se.kjellstrand.webshooter.data.common.CompetitionType
import se.kjellstrand.webshooter.data.common.Logo
import se.kjellstrand.webshooter.data.common.WeaponClass
import se.kjellstrand.webshooter.data.common.WeaponGroup
import se.kjellstrand.webshooter.data.competitions.remote.Datum
import se.kjellstrand.webshooter.data.competitions.remote.ResultsType
import se.kjellstrand.webshooter.data.competitions.remote.Translations
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
    finalTime = finalTime,
    startTimeHuman = startTimeHuman,
    finalTimeHuman = finalTimeHuman,
    signupsCount = signupsCount,
    patrolsCount = patrolsCount,
    allowTeams = allowTeams,
    isPublic = isPublic,
    resultsIsPublic = resultsIsPublic,
    patrolsIsPublic = patrolsIsPublic,
    pdfLogoPath = pdfLogoPath,
    pdfLogoUrl = pdfLogoURL,
    pdfLogo = gson.toJson(pdfLogo),
    competitionTypeJson = gson.toJson(competitionType),
    weaponGroupsJson = gson.toJson(weaponGroups),
    weaponClassesJson = gson.toJson(weaponClasses),
    availableLogosJson = gson.toJson(availableLogos),
    userSignupsJson = gson.toJson(userSignups),
    translationsJson = gson.toJson(translations),
    clubJson = gson.toJson(club)
)

fun CompetitionEntity.toDomain(gson: Gson): Datum? = try {
    Datum(
        id = id,
        championshipsID = 0,
        isPublic = isPublic,
        resultsIsPublic = resultsIsPublic,
        patrolsIsPublic = patrolsIsPublic,
        organizerID = 0,
        organizerType = "",
        invoicesRecipientID = 0,
        invoicesRecipientType = "",
        name = name,
        allowTeams = allowTeams,
        teamsRegistrationFee = 0,
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
        resultsPrices = 0,
        resultsComment = "",
        date = date,
        signupsOpeningDate = signupsOpeningDate ?: "",
        signupsClosingDate = signupsClosingDate ?: "",
        allowSignupsAfterClosingDate = 0,
        priceSignupsAfterClosingDate = 0,
        approvalSignupsAfterClosingDate = 0,
        finalTime = finalTime ?: "",
        createdBy = 0,
        pdfLogo = gson.fromJson<Logo?>(pdfLogo, Logo::class.java) ?: Logo.Webshooter,
        weaponGroups = gson.fromJson<List<WeaponGroup>?>(weaponGroupsJson, object : TypeToken<List<WeaponGroup>>() {}.type) ?: emptyList(),
        signupsCount = signupsCount,
        patrolsCount = patrolsCount,
        status = status,
        statusHuman = statusHuman,
        startTimeHuman = startTimeHuman ?: "",
        finalTimeHuman = finalTimeHuman ?: "",
        allowSignupsAfterClosingDateHuman = allowSignupsAfterClosingDateHuman ?: "",
        translations = if (translationsJson != null) gson.fromJson(translationsJson, Translations::class.java) ?: Translations("","","","","","","","","","","","","","") else Translations("","","","","","","","","","","","","",""),
        resultsTypeHuman = resultsTypeHuman ?: "",
        availableLogos = gson.fromJson<List<Logo>?>(availableLogosJson, object : TypeToken<List<Logo>>() {}.type) ?: emptyList(),
        pdfLogoPath = pdfLogoPath ?: "",
        pdfLogoURL = pdfLogoUrl ?: "",
        competitionType = gson.fromJson(competitionTypeJson, CompetitionType::class.java),
        weaponClasses = gson.fromJson<List<WeaponClass>?>(weaponClassesJson, object : TypeToken<List<WeaponClass>>() {}.type) ?: emptyList(),
        userSignups = gson.fromJson<List<Usersignup>?>(userSignupsJson, object : TypeToken<List<Usersignup>>() {}.type) ?: emptyList(),
        club = gson.fromJson(clubJson, Club::class.java)
    )
} catch (e: JsonSyntaxException) {
    Log.w(TAG, "Error", e)
    null
}
