package se.kjellstrand.webshooter.data.competitions.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface CompetitionsDao {
    @Query("SELECT * FROM competitions ORDER BY date DESC")
    suspend fun getAll(): List<CompetitionEntity>

    @Query("SELECT * FROM competitions WHERE status = 'completed' ORDER BY date DESC")
    suspend fun getCompletedCompetitions(): List<CompetitionEntity>

    @Query("SELECT COUNT(*) FROM competitions WHERE status = 'completed'")
    suspend fun getCompletedCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(competitions: List<CompetitionEntity>)

    @Query("DELETE FROM competitions")
    suspend fun deleteAll()

    @Transaction
    suspend fun replaceAll(competitions: List<CompetitionEntity>) {
        deleteAll()
        insertAll(competitions)
    }
}
