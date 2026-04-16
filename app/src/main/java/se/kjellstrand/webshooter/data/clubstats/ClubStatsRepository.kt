package se.kjellstrand.webshooter.data.clubstats

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import se.kjellstrand.webshooter.data.club.ClubRepository
import se.kjellstrand.webshooter.data.common.Resource
import se.kjellstrand.webshooter.data.common.UserError
import se.kjellstrand.webshooter.data.results.local.ResultsDao
import java.time.Year
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClubStatsRepository @Inject constructor(
    private val clubRepository: ClubRepository,
    private val resultsDao: ResultsDao
) {
    fun getClubStats(): Flow<Resource<ClubStatsData, UserError>> = flow {
        emit(Resource.Loading(true))

        var emittedError = false
        clubRepository.getUserClub().collect { clubResource ->
            when (clubResource) {
                is Resource.Success -> {
                    try {
                        val userIds = clubResource.data.club.users.map { it.userId }
                        val previousYear = Year.now().value - 1
                        val rows = resultsDao.getClubStats(userIds, previousYear)
                        val shooterStats = rows.map { row ->
                            ShooterStats(
                                userId = row.userId,
                                fullname = row.fullname,
                                averagePoints = row.averagePoints,
                                competitionCount = row.competitionCount
                            )
                        }
                        emit(Resource.Success(ClubStatsData(shooterStats)))
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
