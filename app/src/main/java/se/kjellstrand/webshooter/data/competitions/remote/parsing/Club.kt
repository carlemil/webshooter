package se.kjellstrand.webshooter.data.competitions.remote.parsing

import com.beust.klaxon.Json


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
