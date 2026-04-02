package se.kjellstrand.webshooter.data.competitionteams.local

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import se.kjellstrand.webshooter.data.competitionteams.remote.TeamEntry
import se.kjellstrand.webshooter.data.competitionteams.remote.TeamSignupEntry

fun TeamEntry.toEntity(competitionId: Long, gson: Gson): TeamEntity = TeamEntity(
    id = id,
    competitionId = competitionId,
    name = name,
    clubsId = clubsId,
    weapongroupsId = weapongroupsId,
    signupsJson = gson.toJson(signups)
)

fun TeamEntity.toDomain(gson: Gson): TeamEntry = TeamEntry(
    id = id,
    competitionsId = competitionId,
    clubsId = clubsId,
    name = name,
    weapongroupsId = weapongroupsId,
    signups = gson.fromJson(signupsJson, object : TypeToken<List<TeamSignupEntry>>() {}.type)
)
