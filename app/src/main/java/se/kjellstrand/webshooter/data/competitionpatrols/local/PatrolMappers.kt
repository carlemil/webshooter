package se.kjellstrand.webshooter.data.competitionpatrols.local

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import se.kjellstrand.webshooter.data.competitionpatrols.remote.PatrolEntry
import se.kjellstrand.webshooter.data.competitionpatrols.remote.PatrolSignupEntry

fun PatrolEntry.toEntity(competitionId: Long, gson: Gson): PatrolEntity = PatrolEntity(
    id = id,
    competitionId = competitionId,
    sortorder = sortorder,
    startTimeHuman = startTimeHuman,
    endTimeHuman = endTimeHuman,
    signupsJson = gson.toJson(signups)
)

fun PatrolEntity.toDomain(gson: Gson): PatrolEntry = PatrolEntry(
    id = id,
    sortorder = sortorder,
    startTimeHuman = startTimeHuman,
    endTimeHuman = endTimeHuman,
    signups = gson.fromJson(signupsJson, object : TypeToken<List<PatrolSignupEntry>>() {}.type)
)
