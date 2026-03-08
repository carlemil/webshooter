package se.kjellstrand.webshooter.data.results.local

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import se.kjellstrand.webshooter.data.common.WeaponClass
import se.kjellstrand.webshooter.data.results.remote.Result
import se.kjellstrand.webshooter.data.results.remote.Signup
import se.kjellstrand.webshooter.data.results.remote.StationResult
import se.kjellstrand.webshooter.data.results.remote.StdMedal

fun Result.toEntity(gson: Gson): ResultEntity = ResultEntity(
    id = id,
    competitionsId = competitionsID,
    signupsId = signupsID,
    placement = placement,
    figureHits = figureHits,
    hits = hits,
    points = points,
    weaponClassesId = weaponclassesID,
    stdMedal = stdMedal?.name,
    signupJson = gson.toJson(signup),
    weaponClassJson = gson.toJson(weaponClass),
    stationResultsJson = gson.toJson(results)
)

fun ResultEntity.toDomain(gson: Gson): Result = Result(
    id = id,
    competitionsID = competitionsId,
    signupsID = signupsId,
    placement = placement,
    figureHits = figureHits,
    hits = hits,
    points = points,
    weaponclassesID = weaponClassesId,
    stdMedal = stdMedal?.let { StdMedal.fromValue(it) },
    signup = gson.fromJson(signupJson, Signup::class.java),
    weaponClass = gson.fromJson(weaponClassJson, WeaponClass::class.java),
    results = gson.fromJson(stationResultsJson, object : TypeToken<List<StationResult>>() {}.type),
    resultsDistinguish = emptyList(),
    resultsFinals = emptyList()
)
