package se.kjellstrand.webshooter.data.club.remote

import com.google.gson.annotations.SerializedName
import se.kjellstrand.webshooter.data.common.Club

data class ClubInfoResponse(
    val club: Club
)

data class ClubMember(
    val id: Long,
    val name: String,
    val lastname: String? = null,
    val fullname: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val mobile: String? = null,
    @SerializedName("user_has_role") val userHasRole: String? = null
)

data class ClubAdminsResponse(
    val admins: List<ClubMember>
)

data class ClubUsersResponse(
    val users: List<ClubMember>
)
