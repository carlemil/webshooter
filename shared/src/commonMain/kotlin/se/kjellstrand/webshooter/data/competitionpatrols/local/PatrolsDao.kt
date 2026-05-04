package se.kjellstrand.webshooter.data.competitionpatrols.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PatrolsDao {
    @Query("SELECT * FROM patrols WHERE competitionId = :competitionId ORDER BY sortorder")
    suspend fun getByCompetition(competitionId: Long): List<PatrolEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(patrols: List<PatrolEntity>)

    @Query("DELETE FROM patrols WHERE competitionId = :competitionId")
    suspend fun deleteByCompetition(competitionId: Long)
}
