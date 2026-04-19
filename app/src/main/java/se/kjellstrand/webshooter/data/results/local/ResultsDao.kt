package se.kjellstrand.webshooter.data.results.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ResultsDao {
    @Query("SELECT * FROM results WHERE competitionsId = :competitionId")
    suspend fun getByCompetition(competitionId: Long): List<ResultEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(results: List<ResultEntity>)

    @Query("DELETE FROM results WHERE competitionsId = :competitionId")
    suspend fun deleteByCompetition(competitionId: Long)

    @Query(
        """SELECT r.competitionsId AS competitionId,
                  c.name AS competitionName,
                  c.date AS date,
                  CASE c.resultsType
                       WHEN 'PRECISION' THEN 'precision'
                       WHEN 'MILITARY' THEN 'military'
                       WHEN 'FIELD' THEN 'field'
                       WHEN 'POINTS_FIELD' THEN 'pointfield'
                       ELSE c.resultsType
                  END AS resultsType,
                  r.weaponClassName AS weaponClassName,
                  r.userId AS userId,
                  CASE WHEN c.resultsType IN ('FIELD','POINTS_FIELD') THEN r.averageHits ELSE r.averagePoints END AS averageScore
           FROM results r INNER JOIN competitions c ON c.id = r.competitionsId
           WHERE r.userId = :userId AND c.status = 'completed'"""
    )
    suspend fun getChartPointsForUser(userId: Long): List<ChartPointRow>

    @Query(
        """SELECT r.competitionsId AS competitionId,
                  c.name AS competitionName,
                  c.date AS date,
                  CASE c.resultsType
                       WHEN 'PRECISION' THEN 'precision'
                       WHEN 'MILITARY' THEN 'military'
                       WHEN 'FIELD' THEN 'field'
                       WHEN 'POINTS_FIELD' THEN 'pointfield'
                       ELSE c.resultsType
                  END AS resultsType,
                  r.weaponClassName AS weaponClassName,
                  r.userId AS userId,
                  CASE WHEN c.resultsType IN ('FIELD','POINTS_FIELD') THEN r.averageHits ELSE r.averagePoints END AS averageScore
           FROM results r INNER JOIN competitions c ON c.id = r.competitionsId
           WHERE r.userId IN (:userIds) AND r.competitionsId IN (:competitionIds)"""
    )
    suspend fun getChartPointsForUsers(userIds: List<Long>, competitionIds: List<Long>): List<ChartPointRow>

    @Query(
        """SELECT r.competitionsId AS competitionId,
                  c.name AS competitionName,
                  c.date AS date,
                  r.weaponClassName AS weaponClassName,
                  r.stationResultsJson AS stationResultsJson
           FROM results r INNER JOIN competitions c ON c.id = r.competitionsId
           WHERE r.userId = :userId AND c.status = 'completed' AND c.resultsType = 'PRECISION'"""
    )
    suspend fun getPrecisionSeriesForUser(userId: Long): List<SeriesRow>

    @Query("SELECT DISTINCT userId, userFullname AS fullname FROM results ORDER BY userFullname")
    suspend fun getAllParticipants(): List<ParticipantRow>

    @Query(
        """SELECT DISTINCT r.userId, r.userFullname AS fullname
           FROM results r INNER JOIN competitions c ON c.id = r.competitionsId
           WHERE c.status = 'completed' AND c.resultsType = 'PRECISION'
           ORDER BY r.userFullname"""
    )
    suspend fun getPrecisionParticipants(): List<ParticipantRow>

    @Query("SELECT DISTINCT weaponClassName FROM results ORDER BY weaponClassName")
    suspend fun getAllWeaponClasses(): List<String>

    @Query(
        """SELECT r.userId AS userId,
                  r.userFullname AS fullname,
                  AVG(r.averagePoints) AS averagePoints,
                  COUNT(DISTINCT r.competitionsId) AS competitionCount
           FROM results r INNER JOIN competitions c ON c.id = r.competitionsId
           WHERE r.userId IN (:userIds)
             AND c.resultsType = 'PRECISION'
             AND c.status = 'completed'
             AND c.date LIKE :year || '-%'
             AND r.weaponClassName LIKE :classPrefix
           GROUP BY r.userId, r.userFullname"""
    )
    suspend fun getClubStats(userIds: List<Long>, year: Int, classPrefix: String): List<ClubStatsRow>

    @Query(
        """SELECT r.userId AS userId,
                  r.userFullname AS fullname,
                  AVG(r.averagePoints) AS averagePoints,
                  COUNT(DISTINCT r.competitionsId) AS competitionCount
           FROM results r INNER JOIN competitions c ON c.id = r.competitionsId
           WHERE r.userId IN (:userIds)
             AND c.resultsType = 'PRECISION'
             AND c.status = 'completed'
             AND r.weaponClassName LIKE :classPrefix
           GROUP BY r.userId, r.userFullname"""
    )
    suspend fun getClubStatsAllYears(userIds: List<Long>, classPrefix: String): List<ClubStatsRow>

    @Query(
        """SELECT DISTINCT substr(c.date, 1, 4) AS year
           FROM results r INNER JOIN competitions c ON c.id = r.competitionsId
           WHERE r.userId IN (:userIds)
             AND c.resultsType = 'PRECISION'
             AND c.status = 'completed'
           ORDER BY year DESC"""
    )
    suspend fun getClubStatsYears(userIds: List<Long>): List<String>
}
