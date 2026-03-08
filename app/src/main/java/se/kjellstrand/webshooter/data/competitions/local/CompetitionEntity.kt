package se.kjellstrand.webshooter.data.competitions.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "competitions")
data class CompetitionEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val date: String,
    val status: String,
    val statusHuman: String,
    val contactName: String?,
    val contactVenue: String?,
    val contactCity: String?,
    val contactEmail: String?,
    val contactTelephone: String?,
    val lat: Double,
    val lng: Double,
    val googleMaps: String?,
    val description: String?,
    val website: String?,
    val resultsType: String,
    val resultsTypeHuman: String?,
    val signupsOpeningDate: String?,
    val signupsClosingDate: String?,
    val allowSignupsAfterClosingDateHuman: String?,
    val finalTime: String?,
    val startTimeHuman: String?,
    val finalTimeHuman: String?,
    val signupsCount: Long,
    val patrolsCount: Long,
    val allowTeams: Long,
    val isPublic: Long,
    val resultsIsPublic: Long,
    val patrolsIsPublic: Long,
    val pdfLogoPath: String?,
    val pdfLogoUrl: String?,
    val pdfLogo: String?,
    val competitionTypeJson: String,
    val weaponGroupsJson: String,
    val weaponClassesJson: String,
    val availableLogosJson: String,
    val userSignupsJson: String,
    val translationsJson: String?,
    val clubJson: String
)
