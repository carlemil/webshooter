package se.kjellstrand.webshooter.data.competitionteams.local

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import se.kjellstrand.webshooter.data.competitionteams.remote.TeamEntry
import se.kjellstrand.webshooter.data.competitionteams.remote.TeamSignupEntry

fun TeamEntry.toEntity(competitionId: Long, json: Json): TeamEntity = TeamEntity(
    id = id,
    competitionId = competitionId,
    name = name,
    signupsJson = json.encodeToString(signups)
)

fun TeamEntity.toDomain(json: Json): TeamEntry = TeamEntry(
    id = id,
    name = name,
    signups = json.decodeFromString<List<TeamSignupEntry>>(signupsJson)
)
