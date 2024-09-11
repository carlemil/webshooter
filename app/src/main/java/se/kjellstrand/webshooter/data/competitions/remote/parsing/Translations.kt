package se.kjellstrand.webshooter.data.competitions.remote.parsing

import com.beust.klaxon.Json


data class Translations (
    @Json(name = "patrols_name_singular")
    val patrolsNameSingular: String,

    @Json(name = "patrols_name_plural")
    val patrolsNamePlural: String,

    @Json(name = "patrols_list_singular")
    val patrolsListSingular: String,

    @Json(name = "patrols_list_plural")
    val patrolsListPlural: String,

    @Json(name = "patrols_size")
    val patrolsSize: String,

    @Json(name = "patrols_lane_singular")
    val patrolsLaneSingular: String,

    @Json(name = "patrols_lane_plural")
    val patrolsLanePlural: String,

    @Json(name = "stations_name_singular")
    val stationsNameSingular: String,

    @Json(name = "stations_name_plural")
    val stationsNamePlural: String,

    @Json(name = "results_list_singular")
    val resultsListSingular: String,

    @Json(name = "results_list_plural")
    val resultsListPlural: String,

    val shootingcard: String,
    val shootingcards: String,
    val signups: String
)