package se.kjellstrand.webshooter.data.signups.local

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import se.kjellstrand.webshooter.data.signups.remote.SignupCompetition
import se.kjellstrand.webshooter.data.signups.remote.SignupEntry
import se.kjellstrand.webshooter.data.signups.remote.SignupPatrol
import se.kjellstrand.webshooter.data.signups.remote.SignupResultsPlacement
import se.kjellstrand.webshooter.data.signups.remote.SignupTeam
import se.kjellstrand.webshooter.data.signups.remote.SignupWeaponClass

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

fun SignupEntryEntity.toDomain(gson: Gson): SignupEntry = SignupEntry(
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
