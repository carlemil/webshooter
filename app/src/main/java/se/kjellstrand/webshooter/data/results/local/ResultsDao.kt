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
                  c.resultsType AS resultsType,
                  r.weaponClassName AS weaponClassName,
                  r.userId AS userId,
                  CASE WHEN c.resultsType IN ('field','pointfield') THEN r.averageHits ELSE r.averagePoints END AS averageScore
           FROM results r INNER JOIN competitions c ON c.id = r.competitionsId
           WHERE r.userId = :userId AND c.status = 'completed'"""
    )
    suspend fun getChartPointsForUser(userId: Long): List<ChartPointRow>

    @Query(
        """SELECT r.competitionsId AS competitionId,
                  c.name AS competitionName,
                  c.date AS date,
                  c.resultsType AS resultsType,
                  r.weaponClassName AS weaponClassName,
                  r.userId AS userId,
                  CASE WHEN c.resultsType IN ('field','pointfield') THEN r.averageHits ELSE r.averagePoints END AS averageScore
           FROM results r INNER JOIN competitions c ON c.id = r.competitionsId
           WHERE r.userId IN (:userIds) AND r.competitionsId IN (:competitionIds)"""
    )
    suspend fun getChartPointsForUsers(userIds: List<Long>, competitionIds: List<Long>): List<ChartPointRow>

    @Query("SELECT DISTINCT userId, userFullname AS fullname FROM results ORDER BY userFullname")
    suspend fun getAllParticipants(): List<ParticipantRow>

    @Query("SELECT DISTINCT weaponClassName FROM results ORDER BY weaponClassName")
    suspend fun getAllWeaponClasses(): List<String>
}
