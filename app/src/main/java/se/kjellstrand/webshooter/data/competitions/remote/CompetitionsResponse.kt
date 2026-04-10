package se.kjellstrand.webshooter.data.competitions.remote

import com.google.gson.annotations.SerializedName
import se.kjellstrand.webshooter.data.common.Club
import se.kjellstrand.webshooter.data.common.CompetitionType
import se.kjellstrand.webshooter.data.common.WeaponClass
import se.kjellstrand.webshooter.data.common.WeaponGroup

data class CompetitionsResponse (
    val competitions: Competitions
)

data class Competitions (
    @SerializedName("current_page")
    val currentPage: Long,

    val data: List<Datum>,

    @SerializedName("last_page")
    val lastPage: Long,

    val total: Long,
    val status: String,

    @SerializedName("competitiontypes")
    val competitionTypes: List<CompetitionType>
)

data class Datum (
    val id: Long,

    val name: String,

    @SerializedName("allow_teams")
    val allowTeams: Long,

    val website: String? = null,

    @SerializedName("contact_name")
    val contactName: String? = null,

    @SerializedName("contact_venue")
    val contactVenue: String? = null,

    @SerializedName("contact_city")
    val contactCity: String? = null,

    val lat: Double,
    val lng: Double,

    @SerializedName("contact_email")
    val contactEmail: String? = null,

    @SerializedName("contact_telephone")
    val contactTelephone: String? = null,

    @SerializedName("google_maps")
    val googleMaps: String? = null,

    val description: String,

    @SerializedName("results_type")
    val resultsType: ResultsType,

    val date: String,

    @SerializedName("signups_opening_date")
    val signupsOpeningDate: String,

    @SerializedName("signups_closing_date")
    val signupsClosingDate: String,

    @SerializedName("weapongroups")
    val weaponGroups: List<WeaponGroup>,

    @SerializedName("signups_count")
    val signupsCount: Long,

    @SerializedName("patrols_count")
    val patrolsCount: Long,

    @SerializedName("status")
    val status: String,

    @SerializedName("status_human")
    val statusHuman: String,

    @SerializedName("start_time_human")
    val startTimeHuman: String,

    @SerializedName("final_time_human")
    val finalTimeHuman: String,

    @SerializedName("allow_signups_after_closing_date_human")
    val allowSignupsAfterClosingDateHuman: String,

    @SerializedName("results_type_human")
    val resultsTypeHuman: String,

    @SerializedName("competitiontype")
    val competitionType: CompetitionType,

    @SerializedName("weaponclasses")
    val weaponClasses: List<WeaponClass>,

    @SerializedName("usersignups")
    val userSignups: List<Usersignup>,

    val club: Club
)

enum class ResultsType(val apiString: String, val displayName: String) {
    @SerializedName("precision")
    PRECISION("precision", "Precision"),

    @SerializedName("military")
    MILITARY("military", "Militär"),

    @SerializedName("field")
    FIELD("field", "Fält"),

    @SerializedName("pointfield")
    POINTS_FIELD("pointfield", "Poängfält");

    companion object {
        private val byApiString = values().associateBy { it.apiString }
        fun fromApiString(value: String): ResultsType? = byApiString[value]
    }
}

data class Usersignup (
    val id: Long,

    @SerializedName("weaponclasses_id")
    val weaponClassesID: Long,

    @SerializedName("start_time_human")
    val startTimeHuman: String,

    @SerializedName("end_time_human")
    val endTimeHuman: String
)
