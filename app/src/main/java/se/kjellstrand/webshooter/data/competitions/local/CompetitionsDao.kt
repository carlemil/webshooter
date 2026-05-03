package se.kjellstrand.webshooter.data.competitions.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CompetitionsDao {
    @Query("SELECT * FROM competitions ORDER BY date DESC")
    fun observeAll(): Flow<List<CompetitionEntity>>

    @Query("SELECT * FROM competitions WHERE status = 'completed' OR date = :today ORDER BY date DESC")
    suspend fun getCompletedCompetitions(today: String): List<CompetitionEntity>

    @Query("SELECT * FROM competitions")
    suspend fun getAll(): List<CompetitionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(competitions: List<CompetitionEntity>)

    @Query("DELETE FROM competitions WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)
}
