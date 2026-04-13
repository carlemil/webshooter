package se.kjellstrand.webshooter.data.competitions.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface CompetitionsDao {
    @Query("SELECT * FROM competitions ORDER BY date DESC")
    suspend fun getAll(): List<CompetitionEntity>

    @Query("SELECT * FROM competitions ORDER BY date DESC")
    fun observeAll(): Flow<List<CompetitionEntity>>

    @Query("SELECT * FROM competitions WHERE status = 'completed' ORDER BY date DESC")
    suspend fun getCompletedCompetitions(): List<CompetitionEntity>

    @Query("SELECT COUNT(*) FROM competitions WHERE status = 'completed'")
    suspend fun getCompletedCount(): Int

    @Query("SELECT id FROM competitions WHERE status != 'completed'")
    suspend fun getNonCompletedIds(): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(competitions: List<CompetitionEntity>)

    @Query("DELETE FROM competitions")
    suspend fun deleteAll()

    @Query("DELETE FROM competitions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Transaction
    suspend fun replaceAll(competitions: List<CompetitionEntity>) {
        deleteAll()
        insertAll(competitions)
    }
}
