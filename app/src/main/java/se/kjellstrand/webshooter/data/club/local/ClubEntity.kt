package se.kjellstrand.webshooter.data.club.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "club")
data class ClubEntity(
    @PrimaryKey val id: Long,
    val clubsNr: String?,
    val name: String,
    val email: String?,
    val phone: String?,
    val addressStreet: String?,
    val addressZipcode: String?,
    val addressCity: String?,
    val addressCountry: String?,
    val bankgiro: String?,
    val postgiro: String?,
    val swish: String?,
    val adminsJson: String,
    val usersJson: String
)
