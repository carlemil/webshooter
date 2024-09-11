package se.kjellstrand.webshooter.data.competitions.remote.parsing

import com.beust.klaxon.Json


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
