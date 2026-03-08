package se.kjellstrand.webshooter.data.competitions.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CompetitionsDao {
    @Query("SELECT * FROM competitions ORDER BY date DESC")
    suspend fun getAll(): List<CompetitionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(competitions: List<CompetitionEntity>)

    @Query("DELETE FROM competitions")
    suspend fun deleteAll()
}
