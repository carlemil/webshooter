package se.kjellstrand.webshooter.data.competitions.remote

import androidx.annotation.StringRes
import com.beust.klaxon.*
import se.kjellstrand.webshooter.R

private fun <T> Klaxon.convert(k: kotlin.reflect.KClass<*>, fromJson: (JsonValue) -> T, toJson: (T) -> String, isUnion: Boolean = false) =
    this.converter(object: Converter {
        @Suppress("UNCHECKED_CAST")
        override fun toJson(value: Any)        = toJson(value as T)
        override fun fromJson(jv: JsonValue)   = fromJson(jv) as Any
        override fun canConvert(cls: Class<*>) = cls == k.java || (isUnion && cls.superclass == k.java)
    })

private val klaxon = Klaxon()
    .convert(Logo::class,             { Logo.fromValue(it.string!!) },             { "\"${it.value}\"" })
    .convert(ClassnameGeneral::class, { ClassnameGeneral.fromValue(it.string!!) }, { "\"${it.value}\"" })

data class CompetitionsResponse (
    val competitions: Competitions
) {
    public fun toJson() = klaxon.toJsonString(this)

    companion object {
        public fun fromJson(json: String) = klaxon.parse<CompetitionsResponse>(json)
    }
}

data class Competitions (
    @Json(name = "current_page")
    val currentPage: Long,

    val data: List<Datum>,

    @Json(name = "first_page_url")
    val firstPageURL: String,

    val from: Long,

    @Json(name = "last_page")
    val lastPage: Long,

    @Json(name = "last_page_url")
    val lastPageURL: String,

    val links: List<Link>,

    @Json(name = "next_page_url")
    val nextPageURL: Any? = null,

    val path: String,

    @Json(name = "per_page")
    val perPage: Long,

    @Json(name = "prev_page_url")
    val prevPageURL: Any? = null,

    val to: Long,
    val total: Long,
    val search: Any? = null,
    val status: String,

    @Json(name = "clubs_id")
    val clubsID: Any? = null,

    val type: Long,
    val usersignup: Any? = null,
    val competitiontypes: List<Competitiontype>
)

data class Competitiontype (
    val id: Long,
    val name: String,

    @Json(name = "created_at")
    val createdAt: String,

    @Json(name = "updated_at")
    val updatedAt: String,

    @Json(name = "deleted_at")
    val deletedAt: Any? = null
)

data class Datum (
    val id: Long,

    @Json(name = "championships_id")
    val championshipsID: Long,

    @Json(name = "is_public")
    val isPublic: Long,

    @Json(name = "results_is_public")
    val resultsIsPublic: Long,

    @Json(name = "patrols_is_public")
    val patrolsIsPublic: Long,

    @Json(name = "organizer_id")
    val organizerID: Long,

    @Json(name = "organizer_type")
    val organizerType: String,

    @Json(name = "invoices_recipient_id")
    val invoicesRecipientID: Long,

    @Json(name = "invoices_recipient_type")
    val invoicesRecipientType: String,

    val name: String,

    @Json(name = "allow_teams")
    val allowTeams: Long,

    @Json(name = "teams_registration_fee")
    val teamsRegistrationFee: Long,

    val website: String,

    @Json(name = "contact_name")
    val contactName: String,

    @Json(name = "contact_venue")
    val contactVenue: String,

    @Json(name = "contact_city")
    val contactCity: String,

    val lat: Double,
    val lng: Double,

    @Json(name = "contact_email")
    val contactEmail: String,

    @Json(name = "contact_telephone")
    val contactTelephone: String,

    @Json(name = "google_maps")
    val googleMaps: String,

    val description: String,

    @Json(name = "results_type")
    val resultsType: String,

    @Json(name = "results_prices")
    val resultsPrices: Long,

    @Json(name = "results_comment")
    val resultsComment: String,

    val date: String,

    @Json(name = "signups_opening_date")
    val signupsOpeningDate: String,

    @Json(name = "signups_closing_date")
    val signupsClosingDate: String,

    @Json(name = "allow_signups_after_closing_date")
    val allowSignupsAfterClosingDate: Long,

    @Json(name = "price_signups_after_closing_date")
    val priceSignupsAfterClosingDate: Long,

    @Json(name = "approval_signups_after_closing_date")
    val approvalSignupsAfterClosingDate: Long,

    @Json(name = "final_time")
    val finalTime: String,

    @Json(name = "created_by")
    val createdBy: Long,

    @Json(name = "pdf_logo")
    val pdfLogo: Logo,

    @Json(name = "closed_at")
    val closedAt: Any? = null,

    val weapongroups: List<Weapongroup>,

    @Json(name = "signups_count")
    val signupsCount: Long,

    @Json(name = "patrols_count")
    val patrolsCount: Long,

    @Json(name = "status")
    val status: String,

    @Json(name = "status_human")
    val statusHuman: String,

    @Json(name = "start_time_human")
    val startTimeHuman: String,

    @Json(name = "final_time_human")
    val finalTimeHuman: String,

    @Json(name = "allow_signups_after_closing_date_human")
    val allowSignupsAfterClosingDateHuman: String,

    val translations: Translations,

    @Json(name = "results_type_human")
    val resultsTypeHuman: String,

    @Json(name = "available_logos")
    val availableLogos: List<Logo>,

    @Json(name = "pdf_logo_path")
    val pdfLogoPath: String,

    @Json(name = "pdf_logo_url")
    val pdfLogoURL: String,

    val championship: Any? = null,
    val competitiontype: Competitiontype,
    val weaponclasses: List<Weaponclass>,
    val usersignups: List<Usersignup>,
    val club: Club
)

enum class Logo(val value: String) {
    Club("club"),
    Webshooter("webshooter");

    companion object {
        public fun fromValue(value: String): Logo = when (value) {
            "club"       -> Club
            "webshooter" -> Webshooter
            else         -> throw IllegalArgumentException()
        }
    }
}

data class Club (
    val id: Long,

    @Json(name = "disable_personal_invoices")
    val disablePersonalInvoices: Long,

    @Json(name = "districts_id")
    val districtsID: Long,

    @Json(name = "clubs_nr")
    val clubsNr: String,

    val name: String,
    val email: String,
    val phone: String? = null,

    @Json(name = "address_street")
    val addressStreet: String,

    @Json(name = "address_street_2")
    val addressStreet2: String? = null,

    @Json(name = "address_zipcode")
    val addressZipcode: String,

    @Json(name = "address_city")
    val addressCity: String,

    @Json(name = "address_country")
    val addressCountry: String? = null,

    val bankgiro: String? = null,
    val postgiro: String? = null,
    val swish: String,
    val logo: String? = null,

    @Json(name = "user_has_role")
    val userHasRole: Any? = null,

    @Json(name = "address_combined")
    val addressCombined: String,

    @Json(name = "address_incomplete")
    val addressIncomplete: Boolean,

    @Json(name = "logo_url")
    val logoURL: String,

    @Json(name = "logo_path")
    val logoPath: String
)

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

data class Usersignup (
    val id: Long,

    @Json(name = "competitions_id")
    val competitionsID: Long,

    @Json(name = "patrols_id")
    val patrolsID: Long,

    @Json(name = "patrols_finals_id")
    val patrolsFinalsID: Long,

    @Json(name = "lane_finals")
    val laneFinals: Long,

    @Json(name = "patrols_distinguish_id")
    val patrolsDistinguishID: Long,

    @Json(name = "lane_distinguish")
    val laneDistinguish: Long,

    @Json(name = "start_time")
    val startTime: String? = null,

    @Json(name = "end_time")
    val endTime: String? = null,

    val lane: Long,

    @Json(name = "weaponclasses_id")
    val weaponclassesID: Long,

    @Json(name = "registration_fee")
    val registrationFee: Long,

    @Json(name = "invoices_id")
    val invoicesID: Any? = null,

    @Json(name = "clubs_id")
    val clubsID: Long,

    @Json(name = "start_before")
    val startBefore: String,

    @Json(name = "start_after")
    val startAfter: String,

    @Json(name = "first_last_patrol")
    val firstLastPatrol: Any? = null,

    @Json(name = "share_weapon_with")
    val shareWeaponWith: Long,

    @Json(name = "participate_out_of_competition")
    val participateOutOfCompetition: Any? = null,

    @Json(name = "exclude_from_standardmedal")
    val excludeFromStandardmedal: Any? = null,

    @Json(name = "share_patrol_with")
    val sharePatrolWith: Long,

    @Json(name = "shoot_not_simultaneously_with")
    val shootNotSimultaneouslyWith: Long,

    val note: String? = null,

    @Json(name = "requires_approval")
    val requiresApproval: Long,

    @Json(name = "is_approved_by")
    val isApprovedBy: Long,

    @Json(name = "created_by")
    val createdBy: Long,

    @Json(name = "created_at")
    val createdAt: String,

    @Json(name = "special_wishes")
    val specialWishes: String,

    @Json(name = "first_last_patrol_human")
    val firstLastPatrolHuman: String,

    @Json(name = "start_time_human")
    val startTimeHuman: String,

    @Json(name = "end_time_human")
    val endTimeHuman: String
)

data class Weaponclass (
    val id: Long,

    @Json(name = "weapongroups_id")
    val weapongroupsID: Long,

    val classname: String,
    val championship: Long,

    @Json(name = "classname_general")
    val classnameGeneral: ClassnameGeneral,

    val pivot: Pivot
)

enum class ClassnameGeneral(val value: String) {
    A("A"),
    B("B"),
    C("C"),
    CD("CD"),
    CJun("CJun"),
    Cvy("CVY"),
    Cvä("CVÄ"),
    M1("M1"),
    M2("M2"),
    R("R");

    companion object {
        public fun fromValue(value: String): ClassnameGeneral = when (value) {
            "A"    -> A
            "B"    -> B
            "C"    -> C
            "CD"   -> CD
            "CJun" -> CJun
            "CVY"  -> Cvy
            "CVÄ"  -> Cvä
            "M1"   -> M1
            "M2"   -> M2
            "R"    -> R
            else   -> throw IllegalArgumentException()
        }
    }
}

data class Pivot (
    @Json(name = "competitions_id")
    val competitionsID: Long,

    @Json(name = "weaponclasses_id")
    val weaponclassesID: Long,

    @Json(name = "registration_fee")
    val registrationFee: Long
)

data class Weapongroup (
    val id: Long,
    val name: ClassnameGeneral
)

data class Link (
    val url: String? = null,
    val label: String,
    val active: Boolean
)
