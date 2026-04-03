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
}
