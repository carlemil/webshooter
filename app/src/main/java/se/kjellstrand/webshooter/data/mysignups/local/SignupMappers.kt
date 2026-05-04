package se.kjellstrand.webshooter.data.mysignups.local

import io.github.aakira.napier.Napier
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import se.kjellstrand.webshooter.data.mysignups.remote.SignupCompetition
import se.kjellstrand.webshooter.data.mysignups.remote.SignupEntry
import se.kjellstrand.webshooter.data.mysignups.remote.SignupPatrol
import se.kjellstrand.webshooter.data.mysignups.remote.SignupResultsPlacement
import se.kjellstrand.webshooter.data.mysignups.remote.SignupWeaponClass

private const val TAG = "SignupMappers"

fun SignupEntry.toEntity(groupKey: String, json: Json): SignupEntryEntity = SignupEntryEntity(
    id = id,
    groupKey = groupKey,
    competitionsId = competitionsId,
    weaponClassesId = weaponClassesId,
    patrolsId = patrolsId,
    startTime = startTime,
    endTime = endTime,
    lane = lane,
    note = note,
    registrationFee = registrationFee,
    specialWishes = specialWishes,
    startTimeHuman = startTimeHuman,
    endTimeHuman = endTimeHuman,
    competitionJson = json.encodeToString(competition),
    weaponClassJson = json.encodeToString(weaponclass),
    patrolJson = patrol?.let { json.encodeToString(it) },
    resultsPlacementsJson = resultsPlacements?.let { json.encodeToString(it) }
)

fun SignupEntryEntity.toDomain(json: Json): SignupEntry? = try {
    SignupEntry(
        id = id,
        competitionsId = competitionsId,
        weaponClassesId = weaponClassesId,
        patrolsId = patrolsId,
        startTime = startTime,
        endTime = endTime,
        lane = lane,
        note = note,
        registrationFee = registrationFee,
        specialWishes = specialWishes,
        startTimeHuman = startTimeHuman,
        endTimeHuman = endTimeHuman,
        competition = json.decodeFromString<SignupCompetition>(competitionJson),
        weaponclass = json.decodeFromString<SignupWeaponClass>(weaponClassJson),
        patrol = patrolJson?.let { json.decodeFromString<SignupPatrol>(it) },
        resultsPlacements = resultsPlacementsJson?.let { json.decodeFromString<SignupResultsPlacement>(it) }
    )
} catch (e: SerializationException) {
    Napier.w("Error", e, TAG)
    null
}
