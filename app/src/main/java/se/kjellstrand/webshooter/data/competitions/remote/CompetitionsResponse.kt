package se.kjellstrand.webshooter.data.competitions.remote

import com.google.gson.annotations.SerializedName

data class CompetitionsResponse (
    val competitions: Competitions
)

data class Competitions (
    @SerializedName("current_page")
    val currentPage: Long,

    val data: List<Datum>,

    @SerializedName("first_page_url")
    val firstPageURL: String,

    val from: Long,

    @SerializedName("last_page")
    val lastPage: Long,

    @SerializedName("last_page_url")
    val lastPageURL: String,

    val links: List<Link>,

    @SerializedName("next_page_url")
    val nextPageURL: Any? = null,

    val path: String,

    @SerializedName("per_page")
    val perPage: Long,

    @SerializedName("prev_page_url")
    val prevPageURL: Any? = null,

    val to: Long,
    val total: Long,
    val search: Any? = null,
    val status: String,

    @SerializedName("clubs_id")
    val clubsID: Any? = null,

    val type: Long,
    val usersignup: Any? = null,
    val competitiontypes: List<Competitiontype>
)

data class Competitiontype (
    val id: Long,
    val name: String,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("updated_at")
    val updatedAt: String,

    @SerializedName("deleted_at")
    val deletedAt: Any? = null
)

data class Datum (
    val id: Long,

    @SerializedName("championships_id")
    val championshipsID: Long,

    @SerializedName("is_public")
    val isPublic: Long,

    @SerializedName("results_is_public")
    val resultsIsPublic: Long,

    @SerializedName("patrols_is_public")
    val patrolsIsPublic: Long,

    @SerializedName("organizer_id")
    val organizerID: Long,

    @SerializedName("organizer_type")
    val organizerType: String,

    @SerializedName("invoices_recipient_id")
    val invoicesRecipientID: Long,

    @SerializedName("invoices_recipient_type")
    val invoicesRecipientType: String,

    val name: String,

    @SerializedName("allow_teams")
    val allowTeams: Long,

    @SerializedName("teams_registration_fee")
    val teamsRegistrationFee: Long,

    val website: String,

    @SerializedName("contact_name")
    val contactName: String,

    @SerializedName("contact_venue")
    val contactVenue: String,

    @SerializedName("contact_city")
    val contactCity: String,

    val lat: Double,
    val lng: Double,

    @SerializedName("contact_email")
    val contactEmail: String,

    @SerializedName("contact_telephone")
    val contactTelephone: String,

    @SerializedName("google_maps")
    val googleMaps: String,

    val description: String,

    @SerializedName("results_type")
    val resultsType: String,

    @SerializedName("results_prices")
    val resultsPrices: Long,

    @SerializedName("results_comment")
    val resultsComment: String,

    val date: String,

    @SerializedName("signups_opening_date")
    val signupsOpeningDate: String,

    @SerializedName("signups_closing_date")
    val signupsClosingDate: String,

    @SerializedName("allow_signups_after_closing_date")
    val allowSignupsAfterClosingDate: Long,

    @SerializedName("price_signups_after_closing_date")
    val priceSignupsAfterClosingDate: Long,

    @SerializedName("approval_signups_after_closing_date")
    val approvalSignupsAfterClosingDate: Long,

    @SerializedName("final_time")
    val finalTime: String,

    @SerializedName("created_by")
    val createdBy: Long,

    @SerializedName("pdf_logo")
    val pdfLogo: Logo,

    @SerializedName("closed_at")
    val closedAt: Any? = null,

    val weapongroups: List<Weapongroup>,

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

    val translations: Translations,

    @SerializedName("results_type_human")
    val resultsTypeHuman: String,

    @SerializedName("available_logos")
    val availableLogos: List<Logo>,

    @SerializedName("pdf_logo_path")
    val pdfLogoPath: String,

    @SerializedName("pdf_logo_url")
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

    @SerializedName("disable_personal_invoices")
    val disablePersonalInvoices: Long,

    @SerializedName("districts_id")
    val districtsID: Long,

    @SerializedName("clubs_nr")
    val clubsNr: String,

    val name: String,
    val email: String,
    val phone: String? = null,

    @SerializedName("address_street")
    val addressStreet: String,

    @SerializedName("address_street_2")
    val addressStreet2: String? = null,

    @SerializedName("address_zipcode")
    val addressZipcode: String,

    @SerializedName("address_city")
    val addressCity: String,

    @SerializedName("address_country")
    val addressCountry: String? = null,

    val bankgiro: String? = null,
    val postgiro: String? = null,
    val swish: String,
    val logo: String? = null,

    @SerializedName("user_has_role")
    val userHasRole: Any? = null,

    @SerializedName("address_combined")
    val addressCombined: String,

    @SerializedName("address_incomplete")
    val addressIncomplete: Boolean,

    @SerializedName("logo_url")
    val logoURL: String,

    @SerializedName("logo_path")
    val logoPath: String
)

data class Translations (
    @SerializedName("patrols_name_singular")
    val patrolsNameSingular: String,

    @SerializedName("patrols_name_plural")
    val patrolsNamePlural: String,

    @SerializedName("patrols_list_singular")
    val patrolsListSingular: String,

    @SerializedName("patrols_list_plural")
    val patrolsListPlural: String,

    @SerializedName("patrols_size")
    val patrolsSize: String,

    @SerializedName("patrols_lane_singular")
    val patrolsLaneSingular: String,

    @SerializedName("patrols_lane_plural")
    val patrolsLanePlural: String,

    @SerializedName("stations_name_singular")
    val stationsNameSingular: String,

    @SerializedName("stations_name_plural")
    val stationsNamePlural: String,

    @SerializedName("results_list_singular")
    val resultsListSingular: String,

    @SerializedName("results_list_plural")
    val resultsListPlural: String,

    val shootingcard: String,
    val shootingcards: String,
    val signups: String
)

data class Usersignup (
    val id: Long,

    @SerializedName("competitions_id")
    val competitionsID: Long,

    @SerializedName("patrols_id")
    val patrolsID: Long,

    @SerializedName("patrols_finals_id")
    val patrolsFinalsID: Long,

    @SerializedName("lane_finals")
    val laneFinals: Long,

    @SerializedName("patrols_distinguish_id")
    val patrolsDistinguishID: Long,

    @SerializedName("lane_distinguish")
    val laneDistinguish: Long,

    @SerializedName("start_time")
    val startTime: String? = null,

    @SerializedName("end_time")
    val endTime: String? = null,

    val lane: Long,

    @SerializedName("weaponclasses_id")
    val weaponclassesID: Long,

    @SerializedName("registration_fee")
    val registrationFee: Long,

    @SerializedName("invoices_id")
    val invoicesID: Any? = null,

    @SerializedName("clubs_id")
    val clubsID: Long,

    @SerializedName("start_before")
    val startBefore: String,

    @SerializedName("start_after")
    val startAfter: String,

    @SerializedName("first_last_patrol")
    val firstLastPatrol: Any? = null,

    @SerializedName("share_weapon_with")
    val shareWeaponWith: Long,

    @SerializedName("participate_out_of_competition")
    val participateOutOfCompetition: Any? = null,

    @SerializedName("exclude_from_standardmedal")
    val excludeFromStandardmedal: Any? = null,

    @SerializedName("share_patrol_with")
    val sharePatrolWith: Long,

    @SerializedName("shoot_not_simultaneously_with")
    val shootNotSimultaneouslyWith: Long,

    val note: String? = null,

    @SerializedName("requires_approval")
    val requiresApproval: Long,

    @SerializedName("is_approved_by")
    val isApprovedBy: Long,

    @SerializedName("created_by")
    val createdBy: Long,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("special_wishes")
    val specialWishes: String,

    @SerializedName("first_last_patrol_human")
    val firstLastPatrolHuman: String,

    @SerializedName("start_time_human")
    val startTimeHuman: String,

    @SerializedName("end_time_human")
    val endTimeHuman: String
)

data class Weaponclass (
    val id: Long,

    @SerializedName("weapongroups_id")
    val weapongroupsID: Long,

    val classname: String,
    val championship: Long,

    @SerializedName("classname_general")
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
    @SerializedName("competitions_id")
    val competitionsID: Long,

    @SerializedName("weaponclasses_id")
    val weaponclassesID: Long,

    @SerializedName("registration_fee")
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
