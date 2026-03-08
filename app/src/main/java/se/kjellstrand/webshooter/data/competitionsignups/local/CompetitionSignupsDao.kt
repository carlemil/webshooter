package se.kjellstrand.webshooter.data.competitionsignups.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CompetitionSignupsDao {
    @Query("SELECT * FROM competition_signups WHERE competitionId = :competitionId")
    suspend fun getByCompetition(competitionId: Long): List<CompetitionSignupEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(signups: List<CompetitionSignupEntity>)

    @Query("DELETE FROM competition_signups WHERE competitionId = :competitionId")
    suspend fun deleteByCompetition(competitionId: Long)
}
