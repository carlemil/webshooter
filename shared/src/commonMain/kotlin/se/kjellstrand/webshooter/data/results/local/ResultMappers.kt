package se.kjellstrand.webshooter.data.results.local

import io.github.aakira.napier.Napier
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import se.kjellstrand.webshooter.data.common.WeaponClass
import se.kjellstrand.webshooter.data.results.remote.Result
import se.kjellstrand.webshooter.data.results.remote.Signup
import se.kjellstrand.webshooter.data.results.remote.StationResult
import se.kjellstrand.webshooter.data.results.remote.StdMedal

private const val TAG = "ResultMappers"

fun Result.toEntity(competitionId: Long, json: Json): ResultEntity = ResultEntity(
    id = id,
    competitionsId = competitionId,
    signupsId = signupsID,
    placement = placement,
    figureHits = figureHits,
    hits = hits,
    points = points,
    stdMedal = stdMedal?.name,
    signupJson = json.encodeToString(signup),
    weaponClassJson = json.encodeToString(weaponClass),
    stationResultsJson = json.encodeToString(results),
    userId = signup.user.userID,
    userFullname = signup.user.fullname,
    weaponClassName = weaponClass.classname,
    averagePoints = if (results.isEmpty()) 0.0 else results.map { it.points }.average(),
    averageHits = if (results.isEmpty()) 0.0 else results.map { it.hits }.average()
)

fun ResultEntity.toDomain(json: Json): Result? = try {
    Result(
        id = id,
        signupsID = signupsId,
        placement = placement,
        figureHits = figureHits,
        hits = hits,
        points = points,
        stdMedal = stdMedal?.let { StdMedal.fromValue(it) },
        signup = json.decodeFromString<Signup>(signupJson),
        weaponClass = json.decodeFromString<WeaponClass>(weaponClassJson),
        results = json.decodeFromString<List<StationResult>>(stationResultsJson)
    )
} catch (e: SerializationException) {
    Napier.w("Error", e, TAG)
    null
}
