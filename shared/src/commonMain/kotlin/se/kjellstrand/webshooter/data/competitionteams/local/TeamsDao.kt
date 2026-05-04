package se.kjellstrand.webshooter.data.competitionteams.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TeamsDao {
    @Query("SELECT * FROM teams WHERE competitionId = :competitionId ORDER BY name")
    suspend fun getByCompetition(competitionId: Long): List<TeamEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(teams: List<TeamEntity>)

    @Query("DELETE FROM teams WHERE competitionId = :competitionId")
    suspend fun deleteByCompetition(competitionId: Long)
}
