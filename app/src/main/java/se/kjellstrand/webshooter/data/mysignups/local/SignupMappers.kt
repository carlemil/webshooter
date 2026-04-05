package se.kjellstrand.webshooter.data.mysignups.local

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import se.kjellstrand.webshooter.data.mysignups.remote.SignupCompetition
import se.kjellstrand.webshooter.data.mysignups.remote.SignupEntry
import se.kjellstrand.webshooter.data.mysignups.remote.SignupPatrol
import se.kjellstrand.webshooter.data.mysignups.remote.SignupResultsPlacement
import se.kjellstrand.webshooter.data.mysignups.remote.SignupTeam
import se.kjellstrand.webshooter.data.mysignups.remote.SignupWeaponClass

private const val TAG = "SignupMappers"

fun SignupEntry.toEntity(groupKey: String, gson: Gson): SignupEntryEntity = SignupEntryEntity(
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
    competitionJson = gson.toJson(competition),
    weaponClassJson = gson.toJson(weaponclass),
    patrolJson = patrol?.let { gson.toJson(it) },
    teamJson = gson.toJson(team),
    resultsPlacementsJson = resultsPlacements?.let { gson.toJson(it) }
)

fun SignupEntryEntity.toDomain(gson: Gson): SignupEntry? = try {
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
        competition = gson.fromJson(competitionJson, SignupCompetition::class.java),
        weaponclass = gson.fromJson(weaponClassJson, SignupWeaponClass::class.java),
        patrol = patrolJson?.let { gson.fromJson(it, SignupPatrol::class.java) },
        team = gson.fromJson(teamJson, object : TypeToken<List<SignupTeam>>() {}.type),
        resultsPlacements = resultsPlacementsJson?.let { gson.fromJson(it, SignupResultsPlacement::class.java) }
    )
} catch (e: JsonSyntaxException) {
    Log.w(TAG, "Error", e)
    null
}
