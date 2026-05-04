package se.kjellstrand.webshooter.data.competitionpatrols.local

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import se.kjellstrand.webshooter.data.competitionpatrols.remote.PatrolEntry
import se.kjellstrand.webshooter.data.competitionpatrols.remote.PatrolSignupEntry

fun PatrolEntry.toEntity(competitionId: Long, json: Json): PatrolEntity = PatrolEntity(
    id = id,
    competitionId = competitionId,
    sortorder = sortorder,
    startTimeHuman = startTimeHuman,
    endTimeHuman = endTimeHuman,
    signupsJson = json.encodeToString(signups)
)

fun PatrolEntity.toDomain(json: Json): PatrolEntry = PatrolEntry(
    id = id,
    sortorder = sortorder,
    startTimeHuman = startTimeHuman,
    endTimeHuman = endTimeHuman,
    signups = json.decodeFromString<List<PatrolSignupEntry>>(signupsJson)
)
