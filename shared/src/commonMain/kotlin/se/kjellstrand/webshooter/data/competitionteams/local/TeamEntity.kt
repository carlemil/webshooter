package se.kjellstrand.webshooter.data.competitionteams.local

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "teams",
    primaryKeys = ["id", "competitionId"],
    indices = [Index("competitionId")]
)
data class TeamEntity(
    val id: Long,
    val competitionId: Long,
    val name: String,
    val signupsJson: String
)
