package se.kjellstrand.webshooter.data.charts

import android.util.Log
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import se.kjellstrand.webshooter.data.common.Resource
import java.time.LocalDate
import se.kjellstrand.webshooter.data.common.UserError
import se.kjellstrand.webshooter.data.competitions.remote.CompetitionsRemoteDataSource
import se.kjellstrand.webshooter.data.competitions.remote.Datum
import se.kjellstrand.webshooter.data.competitions.remote.ResultsType
import se.kjellstrand.webshooter.data.results.ResultsRepository
import se.kjellstrand.webshooter.data.results.remote.Result
import se.kjellstrand.webshooter.data.results.remote.ResultsResponse

import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

data class ChartDataPoint(
    val competitionId: Long,
    val competitionName: String,
    val date: String,
    val averageSerieScore: Double,
    val weaponClass: String,
    val resultsType: String
)

data class Participant(
    val userId: Long,
    val fullname: String
)

data class ChartData(
    val dataPoints: List<ChartDataPoint>,
    val allWeaponClasses: List<String> = emptyList(),
    val allParticipants: List<Participant> = emptyList(),
    val allCompetitionMeta: Map<Long, CompetitionMeta> = emptyMap()
)

data class CompetitionMeta(
    val name: String,
    val date: String,
    val resultsType: String
)

@Singleton
class ChartsRepository @Inject constructor(
    private val competitionsRemoteDataSource: CompetitionsRemoteDataSource,
    private val resultsRepository: ResultsRepository
) {
    companion object {
        private const val TAG = "ChartsRepository"
    }

    private val resultsCache = ConcurrentHashMap<Long, ResultsResponse>()
    private var competitionsCache: List<Datum>? = null

    fun getChartData(userId: Long): Flow<Resource<ChartData, UserError>> = channelFlow {
        send(Resource.Loading(true))

        val competitions: List<Datum> = competitionsCache ?: try {
            fetchAllCompetitions().also { competitionsCache = it }
        } catch (e: Exception) {
            Log.w(TAG, "Error fetching competitions", e)
            send(Resource.Error(UserError.UnknownError))
            send(Resource.Loading(false))
            return@channelFlow
        }

        val allCompetitionMeta = mutableMapOf<Long, CompetitionMeta>()
        val competitionsWithType = competitions.mapNotNull { competition ->
            val resultsType = try {
                competition.resultsType.toApiString()
            } catch (e: Exception) {
                Log.w(TAG, "Unknown resultsType for competition ${competition.id}, skipping")
                return@mapNotNull null
            }
            allCompetitionMeta[competition.id] = CompetitionMeta(
                name = competition.name,
                date = competition.date,
                resultsType = resultsType
            )
            competition to resultsType
        }

        // Emit an initial Success containing only metadata so the UI can render
        // the chart frame and tabs immediately. Datapoints stream in below.
        send(
            Resource.Success(
                ChartData(
                    dataPoints = emptyList(),
                    allWeaponClasses = emptyList(),
                    allParticipants = emptyList(),
                    allCompetitionMeta = allCompetitionMeta.toMap()
                )
            )
        )

        // Shared aggregation state, guarded by a mutex because each completed
        // fetch updates it from its own coroutine before sending a snapshot.
        val mutex = Mutex()
        val dataPoints = mutableListOf<ChartDataPoint>()
        val allWeaponClasses = sortedSetOf<String>()
        val allParticipants = mutableMapOf<Long, String>()

        // Wait for all per-competition fetches inside coroutineScope so that
        // the channelFlow block does not return (and close the channel) until
        // every child has finished sending its progressive snapshot.
        try {
            coroutineScope {
                val semaphore = Semaphore(6)
                for ((competition, resultsType) in competitionsWithType) {
                    delay(500)
                    launch {
                        val resultsResult: Resource<ResultsResponse, UserError>? = semaphore.withPermit {
                            fetchResults(competition.id, competition.date)
                        }
                        if (resultsResult !is Resource.Success) return@launch

                        val snapshot: ChartData = mutex.withLock {
                            resultsResult.data.results.forEach { result ->
                                allWeaponClasses.add(result.weaponClass.classname)
                                val user = result.signup.user
                                allParticipants.putIfAbsent(user.userID, user.fullname)
                            }
                            resultsResult.data.results
                                .filter { it.signup.user.userID == userId }
                                .forEach { result ->
                                    val avg = computeAverageScore(result, resultsType)
                                    dataPoints.add(
                                        ChartDataPoint(
                                            competitionId = competition.id,
                                            competitionName = competition.name,
                                            date = competition.date,
                                            averageSerieScore = avg,
                                            weaponClass = result.weaponClass.classname,
                                            resultsType = resultsType
                                        )
                                    )
                                }
                            ChartData(
                                dataPoints = dataPoints.sortedBy { it.date },
                                allWeaponClasses = allWeaponClasses.toList(),
                                allParticipants = allParticipants.map { (id, name) ->
                                    Participant(userId = id, fullname = name)
                                }.sortedBy { it.fullname },
                                allCompetitionMeta = allCompetitionMeta.toMap()
                            )
                        }
                        send(Resource.Success(snapshot))
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error fetching chart results", e)
            send(Resource.Error(UserError.UnknownError))
        }

        send(Resource.Loading(false))
    }

    /**
     * Returns cached results if available, otherwise fetches from Room/network.
     * For past competitions, results are final so we use cache-first and never
     * re-fetch once cached. For today's competitions, we always refresh from
     * the network to pick up live result updates.
     */
    private suspend fun fetchResults(
        competitionId: Long,
        competitionDate: String
    ): Resource<ResultsResponse, UserError>? {
        val cached = resultsCache[competitionId]
        val isToday = try {
            LocalDate.parse(competitionDate) == LocalDate.now()
        } catch (e: Exception) {
            false
        }

        if (cached != null && !isToday) {
            return Resource.Success(cached)
        }

        val result = runCatching {
            val repo = if (isToday) {
                resultsRepository.get(competitionId)
            } else {
                resultsRepository.getPreferCached(competitionId)
            }
            lastNonLoading(repo)
        }.getOrElse { e ->
            Log.w(TAG, "Error fetching results for $competitionId", e)
            null
        }

        if (result is Resource.Success) {
            resultsCache[competitionId] = result.data
        }
        return result
    }

    private suspend fun fetchAllCompetitions(): List<Datum> {
        val competitions = mutableListOf<Datum>()

        var page = 1
        var hasMore = true
        while (hasMore) {
            val response = competitionsRemoteDataSource.getCompetitions(
                page = page,
                perPage = 10,
                status = "all",
                type = 0,
                userSignup = 0
            )
            val pageData = response.competitions.data
            competitions.addAll(pageData)
            hasMore = pageData.size >= 10 && page < response.competitions.lastPage
            page++
        }
        return competitions
    }

    fun getShooterChartData(
        shooterIds: List<Long>,
        competitionIds: List<Long>,
        competitionMetadata: Map<Long, CompetitionMeta>
    ): Flow<Resource<Map<Long, ChartData>, UserError>> = flow {
        emit(Resource.Loading(true))

        val result = mutableMapOf<Long, MutableList<ChartDataPoint>>()
        shooterIds.forEach { result[it] = mutableListOf() }

        for (competitionId in competitionIds) {
            val meta = competitionMetadata[competitionId]
            val resultsResult = fetchResults(competitionId, meta?.date ?: "")

            if (resultsResult is Resource.Success) {
                for (shooterId in shooterIds) {
                    val shooterResults = resultsResult.data.results.filter {
                        it.signup.user.userID == shooterId
                    }
                    for (r in shooterResults) {
                        val resultsType = meta?.resultsType ?: ""
                        val avg = computeAverageScore(r, resultsType)
                        result[shooterId]?.add(
                            ChartDataPoint(
                                competitionId = r.competitionsID,
                                competitionName = meta?.name ?: "",
                                date = meta?.date ?: "",
                                averageSerieScore = avg,
                                weaponClass = r.weaponClass.classname,
                                resultsType = resultsType
                            )
                        )
                    }
                }
            }
        }

        emit(
            Resource.Success(
                result.mapValues { (_, points) ->
                    ChartData(dataPoints = points.sortedBy { it.date })
                }
            )
        )
        emit(Resource.Loading(false))
    }

    private fun computeAverageScore(result: Result, resultsType: String): Double {
        val stations = result.results
        if (stations.isEmpty()) return 0.0
        return if (resultsType == "field" || resultsType == "pointfield") {
            stations.map { it.hits }.average()
        } else {
            stations.map { it.points }.average()
        }
    }

    private suspend fun <T, E : se.kjellstrand.webshooter.data.common.Error> lastNonLoading(
        flow: Flow<Resource<T, E>>
    ): Resource<T, E>? {
        var last: Resource<T, E>? = null
        flow.collect { resource ->
            if (resource !is Resource.Loading) {
                last = resource
            }
        }
        return last
    }

    private fun ResultsType.toApiString(): String = apiString
}
