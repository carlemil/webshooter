package se.kjellstrand.webshooter.data.competitionsignups.local

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "competition_signups",
    primaryKeys = ["id", "competitionId"],
    indices = [Index("competitionId")]
)
data class CompetitionSignupEntity(
    val id: Long,
    val competitionId: Long,
    val userJson: String,
    val clubJson: String,
    val weaponClassJson: String
)
