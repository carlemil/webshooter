package se.kjellstrand.webshooter.data.results.local

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import se.kjellstrand.webshooter.data.common.WeaponClass
import se.kjellstrand.webshooter.data.results.remote.Result
import se.kjellstrand.webshooter.data.results.remote.Signup
import se.kjellstrand.webshooter.data.results.remote.StationResult
import se.kjellstrand.webshooter.data.results.remote.StdMedal

private const val TAG = "ResultMappers"

fun Result.toEntity(competitionId: Long, gson: Gson): ResultEntity = ResultEntity(
    id = id,
    competitionsId = competitionId,
    signupsId = signupsID,
    placement = placement,
    figureHits = figureHits,
    hits = hits,
    points = points,
    stdMedal = stdMedal?.name,
    signupJson = gson.toJson(signup),
    weaponClassJson = gson.toJson(weaponClass),
    stationResultsJson = gson.toJson(results)
)

fun ResultEntity.toDomain(gson: Gson): Result? = try {
    Result(
        id = id,
        signupsID = signupsId,
        placement = placement,
        figureHits = figureHits,
        hits = hits,
        points = points,
        stdMedal = stdMedal?.let { StdMedal.fromValue(it) },
        signup = gson.fromJson(signupJson, Signup::class.java),
        weaponClass = gson.fromJson(weaponClassJson, WeaponClass::class.java),
        results = gson.fromJson(stationResultsJson, object : TypeToken<List<StationResult>>() {}.type)
    )
} catch (e: JsonSyntaxException) {
    Log.w(TAG, "Error", e)
    null
}
