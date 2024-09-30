package se.kjellstrand.webshooter.data.common

import com.google.gson.annotations.SerializedName


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