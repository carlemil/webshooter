package se.kjellstrand.webshooter.data.competitions.remote.parsing

import com.beust.klaxon.Json


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
