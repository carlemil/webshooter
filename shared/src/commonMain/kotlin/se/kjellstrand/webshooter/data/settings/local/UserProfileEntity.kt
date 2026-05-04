package se.kjellstrand.webshooter.data.settings.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val userId: Long,
    val name: String,
    val lastname: String,
    val email: String,
    val shootingCardNumber: String?,
    val noShootingCardNumber: String?,
    val birthday: String?,
    val gender: String?,
    val phone: String?,
    val mobile: String?,
    val gradeField: String?,
    val gradeTrackshooting: String?,
    val apiToken: String?,
    val fullname: String,
    val clubsId: Long,
    val status: String,
    val clubsJson: String
)
