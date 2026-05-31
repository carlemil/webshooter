package se.kjellstrand.webshooter.data.competitions.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import se.kjellstrand.webshooter.data.common.Club
import se.kjellstrand.webshooter.data.common.CompetitionType
import se.kjellstrand.webshooter.data.common.WeaponClass
import se.kjellstrand.webshooter.data.common.WeaponGroup

@Serializable
data class CompetitionsResponse (
    val competitions: Competitions
)

@Serializable
data class Competitions (
    @SerialName("current_page")
    val currentPage: Long,

    val data: List<Datum>,

    @SerialName("last_page")
    val lastPage: Long,

    val total: Long,
    val status: String = "",

    @SerialName("competitiontypes")
    val competitionTypes: List<CompetitionType> = emptyList()
)

@Serializable
data class Datum (
    val id: Long,

    val name: String,

    @SerialName("allow_teams")
    val allowTeams: Long,

    val website: String? = null,

    @SerialName("contact_name")
    val contactName: String? = null,

    @SerialName("contact_venue")
    val contactVenue: String? = null,

    @SerialName("contact_city")
    val contactCity: String? = null,

    val lat: Double? = null,
    val lng: Double? = null,

    @SerialName("contact_email")
    val contactEmail: String? = null,

    @SerialName("contact_telephone")
    val contactTelephone: String? = null,

    @SerialName("google_maps")
    val googleMaps: String? = null,

    val description: String = "",

    @SerialName("results_type")
    val resultsType: ResultsType,

    val date: String,

    @SerialName("signups_opening_date")
    val signupsOpeningDate: String,

    @SerialName("signups_closing_date")
    val signupsClosingDate: String,

    @SerialName("weapongroups")
    val weaponGroups: List<WeaponGroup>,

    @SerialName("signups_count")
    val signupsCount: Long,

    @SerialName("patrols_count")
    val patrolsCount: Long,

    @SerialName("status")
    val status: String,

    @SerialName("status_human")
    val statusHuman: String,

    @SerialName("start_time_human")
    val startTimeHuman: String,

    @SerialName("final_time_human")
    val finalTimeHuman: String,

    @SerialName("allow_signups_after_closing_date_human")
    val allowSignupsAfterClosingDateHuman: String,

    @SerialName("results_type_human")
    val resultsTypeHuman: String,

    @SerialName("competitiontype")
    val competitionType: CompetitionType,

    @SerialName("weaponclasses")
    val weaponClasses: List<WeaponClass>,

    @SerialName("usersignups")
    val userSignups: List<Usersignup>,

    val club: Club
)

@Serializable
enum class ResultsType(val apiString: String, val displayName: String) {
    @SerialName("precision")
    PRECISION("precision", "Precision"),

    @SerialName("military")
    MILITARY("military", "Militär"),

    @SerialName("field")
    FIELD("field", "Fält"),

    @SerialName("pointfield")
    POINTS_FIELD("pointfield", "Poängfält");

    companion object {
        private val byApiString = values().associateBy { it.apiString }
        fun fromApiString(value: String): ResultsType? = byApiString[value]
    }
}

@Serializable
data class Usersignup (
    val id: Long,

    @SerialName("weaponclasses_id")
    val weaponClassesID: Long,

    @SerialName("start_time_human")
    val startTimeHuman: String,

    @SerialName("end_time_human")
    val endTimeHuman: String
)
