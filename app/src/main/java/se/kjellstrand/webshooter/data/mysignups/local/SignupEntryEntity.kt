package se.kjellstrand.webshooter.data.mysignups.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "signups")
data class SignupEntryEntity(
    @PrimaryKey val id: Long,
    val groupKey: String,
    val competitionsId: Long,
    val weaponClassesId: Long,
    val patrolsId: Long,
    val startTime: String?,
    val endTime: String?,
    val lane: Long,
    val note: String?,
    val registrationFee: Long,
    val specialWishes: String,
    val startTimeHuman: String,
    val endTimeHuman: String,
    val competitionJson: String,
    val weaponClassJson: String,
    val patrolJson: String?,
    val teamJson: String,
    val resultsPlacementsJson: String?
)
