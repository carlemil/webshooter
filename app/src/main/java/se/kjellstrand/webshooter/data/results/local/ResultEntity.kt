package se.kjellstrand.webshooter.data.results.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "results",
    indices = [Index(value = ["userId", "competitionsId"])]
)
data class ResultEntity(
    @PrimaryKey val id: Long,
    val competitionsId: Long,
    val signupsId: Long,
    val placement: Long,
    val figureHits: Long,
    val hits: Long,
    val points: Long,
    val stdMedal: String?,
    val signupJson: String,
    val weaponClassJson: String,
    val stationResultsJson: String,
    val userId: Long = 0L,
    val userFullname: String = "",
    val weaponClassName: String = "",
    val averagePoints: Double = 0.0,
    val averageHits: Double = 0.0
)
