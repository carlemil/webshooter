package se.kjellstrand.webshooter.data.clubstats

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDate
import se.kjellstrand.webshooter.data.club.ClubRepository
import se.kjellstrand.webshooter.data.common.Resource
import se.kjellstrand.webshooter.data.common.UserError
import se.kjellstrand.webshooter.data.results.local.ResultsDao
import se.kjellstrand.webshooter.ui.screens.charts.seriespoints.WeaponClassGroup
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClubStatsRepository @Inject constructor(
    private val clubRepository: ClubRepository,
    private val resultsDao: ResultsDao
) {
    fun getClubStats(
        group: WeaponClassGroup? = null,
        year: Int? = null
    ): Flow<Resource<ClubStatsData, UserError>> = flow {
        emit(Resource.Loading(true))

        val classPrefix = when (group) {
            null -> "%"
            else -> "${group.prefix}%"
        }

        val today = LocalDate.now().toString()
        var emittedError = false
        clubRepository.getUserClub().collect { clubResource ->
            when (clubResource) {
                is Resource.Success -> {
                    try {
                        val userIds = clubResource.data.club.users.map { it.userId }
                        val rows = if (year == null) {
                            resultsDao.getClubStatsAllYears(userIds, classPrefix, today)
                        } else {
                            resultsDao.getClubStats(userIds, year, classPrefix, today)
                        }
                        val shooterStats = rows
                            .filter { it.averagePoints > 0.0 }
                            .map { row ->
                                ShooterStats(
                                    userId = row.userId,
                                    fullname = row.fullname,
                                    averagePoints = row.averagePoints,
                                    competitionCount = row.competitionCount
                                )
                            }
                            .sortedBy { it.fullname.lowercase() }
                        val availableYears = resultsDao.getClubStatsYears(userIds, today)
                            .mapNotNull { it.toIntOrNull() }
                        emit(Resource.Success(ClubStatsData(shooterStats, availableYears)))
                    } catch (e: Exception) {
                        emit(Resource.Error(UserError.UnknownError))
                        emittedError = true
                    }
                }
                is Resource.Error -> {
                    if (!emittedError) {
                        emit(Resource.Error(clubResource.error))
                        emittedError = true
                    }
                }
                is Resource.Loading -> { /* handled by outer Loading emissions */ }
            }
        }

        emit(Resource.Loading(false))
    }
}
